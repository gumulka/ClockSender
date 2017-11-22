package de.gummu.clockjava;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by pflug on 22.11.17.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
