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
 * Copyright 2015-2016 ForgeRock AS.
 *
 * Portions Copyright 2013 Nathaniel McCallum, Red Hat
 */

package com.forgerock.authenticator;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity {

    private static final String[][] credits = new String[][] {
            { "ZXing Core", "Apache License 2.0", "https://github.com/zxing/zxing/"},
            { "Gson", "Apache License 2.0", "https://github.com/google/gson"},
            { "Picasso", "Apache License 2.0", "https://github.com/square/picasso"},
            { "Roboguice", "Apache License 2.0", "https://github.com/roboguice/roboguice"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }

    @Override
    public void onStart() {
        super.onStart();

        Resources res = getResources();
        TextView tv;

        try {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), 0);
            String version = res.getString(R.string.about_version, info.versionName, info.versionCode);

            StringBuilder aboutBuilder = new StringBuilder(getString(R.string.about_licenses_title));

            for (String[] values : credits) {
                aboutBuilder.append(String.format("%1$s - %2$s (<a href = \"\" + %3$s + \"\">%4$s</a>)", values[0], values[1], values[2], getString(R.string.about_text_website)));
                aboutBuilder.append("<br />");
            }

            tv = (TextView) findViewById(R.id.about_version);
            tv.setText(version);

            TextView credits = (TextView) findViewById(R.id.credits);
            credits.setText(Html.fromHtml(aboutBuilder.toString()));
            credits.setMovementMethod(LinkMovementMethod.getInstance());

            TextView about = (TextView) findViewById(R.id.about);
            about.setText(Html.fromHtml(getString(R.string.about)));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }
}
