/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

package com.forgerock.authenticator.add;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.mechanisms.CoreMechanismFactory;
import com.forgerock.authenticator.mechanisms.DuplicateMechanismException;
import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.URIMappingException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.storage.IdentityModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roboguice.RoboGuice;

/**
 * Class responsible for making a request to OpenAM to complete the authentication request.
 */
public class CreateMechanismFromUriTask extends AsyncTask<String, Void, Mechanism> {
    private Activity activity;
    private MechanismPostRunnable onCompletion;
    private Mechanism duplicate = null;
    private String[] uri;
    private Logger logger = LoggerFactory.getLogger(CreateMechanismFromUriTask.class);

    /**
     * Creates the task.
     */
    public CreateMechanismFromUriTask(Activity activity, MechanismPostRunnable onCompletion) {
        this.activity = activity;
        this.onCompletion = onCompletion;
    }

    @Override
    protected Mechanism doInBackground(String... uri) {
        this.uri = uri;
        if (uri.length != 1) {
            return null;
        }
        IdentityModel model = RoboGuice.getInjector(activity).getInstance(IdentityModel.class);
        try {
            Mechanism mechanism = new CoreMechanismFactory(activity, model).createFromUri(uri[0]);
            return mechanism;
        } catch (DuplicateMechanismException e) {
            duplicate = e.getCausingMechanism();
        } catch (MechanismCreationException | URIMappingException e) {
            logger.error("Failed to create mechanism from URI", e);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, R.string.invalid_qr, Toast.LENGTH_SHORT).show();
                }
            });
        }
        return null;
    }

    @Override
    protected void onPostExecute(Mechanism mechanism) {
        if (duplicate != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Duplicate detected")
                    .setIcon(R.drawable.forgerock_placeholder)
                    .setMessage("Warning! This will replace an existing login mechanism. This operation cannot be undone. You should only proceed if you were expecting to update a mechanism.")
                    .setPositiveButton("Replace", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            duplicate.getOwner().removeMechanism(duplicate);
                            new CreateMechanismFromUriTask(activity, onCompletion).execute(uri);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            complete(null);
                        }
                    });

            builder.create().show();
        }
        complete(mechanism);
    }

    private void complete(Mechanism mechanism) {
        if (onCompletion != null) {
            onCompletion.run(mechanism);
        }
    }

    public interface MechanismPostRunnable {

        /**
         * Run any post execution on the Mechanism that was produced by a CreateMechanismFromUriTask
         * @param mechanism The mechanism that was produced. Will be null if creation failed.
         */
        void run(Mechanism mechanism);
    }
}