package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

class EditorProfileListViewHolder extends RecyclerView.ViewHolder
                    implements View.OnClickListener, View.OnLongClickListener

{

    final DragHandle dragHandle;
    //private RelativeLayout listItemRoot;
    private final ImageView profileIcon;
    private final TextView profileName;
    private ImageView profileIndicator;
    private final ImageView profileItemEditMenu;

    private Profile profile;
    private final EditorProfileListFragment editorFragment;

    private final Context context;

    EditorProfileListViewHolder(View itemView, EditorProfileListFragment editorFragment, Context context, int filterType) {
        super(itemView);

        this.context = context;
        this.editorFragment = editorFragment;

        if (filterType == EditorProfileListFragment.FILTER_TYPE_SHOW_IN_ACTIVATOR)
            dragHandle = itemView.findViewById(R.id.profile_list_drag_handle);
        else
            dragHandle = null;

        //listItemRoot = itemView.findViewById(R.id.profile_list_item_root);
        profileName = itemView.findViewById(R.id.profile_list_item_profile_name);
        profileIcon = itemView.findViewById(R.id.profile_list_item_profile_icon);
        profileItemEditMenu = itemView.findViewById(R.id.profile_list_item_edit_menu);
        if (ApplicationPreferences.applicationEditorPrefIndicator(context))
            profileIndicator = itemView.findViewById(R.id.profile_list_profile_pref_indicator);

        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

    }

    void bindProfile(Profile profile) {

        this.profile = profile;

        if (profile._checked && (!ApplicationPreferences.applicationEditorHeader(context)))
        {
            profileName.setTypeface(null, Typeface.BOLD);
            profileName.setTextSize(16);
            profileName.setTextColor(GlobalGUIRoutines.getThemeAccentColor(editorFragment.getActivity()));
        }
        else
        {
            profileName.setTypeface(null, Typeface.NORMAL);
            profileName.setTextSize(15);
            profileName.setTextColor(GlobalGUIRoutines.getThemeTextColor(editorFragment.getActivity()));
        }

        String _profileName = DataWrapper.getProfileNameWithManualIndicator(profile,
                                    profile._checked &&
                                    (!ApplicationPreferences.applicationEditorHeader(context)), true, false,
                                    editorFragment.activityDataWrapper);
        //Log.d("EditorProfileListViewHolder.bindProfile","_profileName="+_profileName);
        if (profile._showInActivator)
            _profileName = "[A] " + _profileName;

        profileName.setText(_profileName);

        if (profile.getIsIconResourceID())
        {
            if (profile._iconBitmap != null)
                profileIcon.setImageBitmap(profile._iconBitmap);
            else {
                //holder.profileIcon.setImageBitmap(null);
                int res = context.getResources().getIdentifier(profile.getIconIdentifier(), "drawable",
                        context.getPackageName());
                profileIcon.setImageResource(res); // icon resource
            }
        }
        else
        {
            profileIcon.setImageBitmap(profile._iconBitmap);
        }

        if (ApplicationPreferences.applicationEditorPrefIndicator(context))
        {
            //profilePrefIndicatorImageView.setImageBitmap(null);
            //Bitmap bitmap = ProfilePreferencesIndicator.paint(profile, vi.getContext());
            //profilePrefIndicatorImageView.setImageBitmap(bitmap);
            if (profileIndicator != null)
                profileIndicator.setImageBitmap(profile._preferencesIndicator);
        }

        profileItemEditMenu.setTag(profile);
        profileItemEditMenu.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                editorFragment.showEditMenu(profileItemEditMenu);
            }
        });
    }

    @Override
    public void onClick(View v) {
        editorFragment.startProfilePreferencesActivity(profile, 0);
    }

    @Override
    public boolean onLongClick(View v) {
        editorFragment.activateProfile(profile);
        return true;
    }

}
