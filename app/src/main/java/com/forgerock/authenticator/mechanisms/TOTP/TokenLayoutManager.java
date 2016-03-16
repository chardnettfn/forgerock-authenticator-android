package com.forgerock.authenticator.mechanisms.TOTP;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.forgerock.authenticator.R;
import com.forgerock.authenticator.edit.DeleteActivity;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.mechanisms.Mechanism;
import com.forgerock.authenticator.mechanisms.MechanismLayoutManager;

public class TokenLayoutManager extends MechanismLayoutManager{
    @Override
    public void bind(final Context context, Mechanism mechanism, View view) {
        final Token thisToken = (Token) mechanism;
        TokenLayout tl = (TokenLayout) view;
        tl.bind(thisToken, R.menu.token, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent i;

                switch (item.getItemId()) {

                    case R.id.action_delete:
                        i = new Intent(context, DeleteActivity.class);
                        i.putExtra(DeleteActivity.ROW_ID, thisToken.getRowId());
                        context.startActivity(i);
                        break;
                }

                return true;
            }
        });

        tl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Increment the token.
                TokenCode codes = thisToken.generateCodes();
                new IdentityDatabase(v.getContext()).updateMechanism(thisToken);

                ((TokenLayout) v).start(thisToken.getType(), codes, true);
            }
        });
    }

    @Override
    public int getLayoutType() {
        return R.layout.token;
    }
}
