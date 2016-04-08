package com.forgerock.authenticator.identity;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.forgerock.authenticator.MechanismActivity;
import com.forgerock.authenticator.R;
import com.forgerock.authenticator.delete.DeleteIdentityActivity;
import com.forgerock.authenticator.ui.MechanismIcon;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Individual entry which displays information about a given Identity.
 */
public class IdentityLayout extends RelativeLayout {

    public IdentityLayout(Context context) {
        super(context);
    }

    public IdentityLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IdentityLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Set the Identity that this Layout displays.
     * @param identity The Identity to display.
     */
    void bind(final Identity identity) {
        TextView issuerView = (TextView) findViewById(R.id.issuer);
        TextView labelView = (TextView) findViewById(R.id.label);
        ImageView imageView = (ImageView) findViewById(R.id.image);
        issuerView.setText(identity.getIssuer());
        labelView.setText(identity.getAccountName());

        Picasso.with(getContext())
                .load(identity.getImage())
                .placeholder(R.drawable.forgerock_placeholder)
                .into(imageView);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MechanismActivity.class);
                intent.putExtra(MechanismActivity.IDENTITY_REFERENCE, identity.getOpaqueReference());
                getContext().startActivity(intent);
            }
        });

        ImageView mMenu = (ImageView) findViewById(R.id.menu);

        final PopupMenu popupMenu = new PopupMenu(getContext(), mMenu);
        mMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        final Context context = getContext();
        popupMenu.getMenu().clear();
        popupMenu.getMenuInflater().inflate(R.menu.token, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i;

                switch (item.getItemId()) {

                    case R.id.action_delete:
                        i = new Intent(context, DeleteIdentityActivity.class);
                        i.putExtra(DeleteIdentityActivity.IDENTITY_REFERENCE, identity.getOpaqueReference());
                        context.startActivity(i);
                        break;
                }

                return true;
            }
        });

        // There should currently only be at most two mechanisms associated with the Identity
        List<MechanismIcon> icons = new ArrayList<>();
        icons.add((MechanismIcon) findViewById(R.id.iconA));
        icons.add((MechanismIcon) findViewById(R.id.iconB));

        for (int i = 0; i < icons.size(); i++) {
            icons.get(i).setVisibility(GONE);
        }

        for (int i = 0; i < identity.getMechanisms().size() && i < icons.size(); i++) {
            icons.get(i).setVisibility(VISIBLE);
            icons.get(i).setMechanism(identity.getMechanisms().get(i));
        }


    }
}
