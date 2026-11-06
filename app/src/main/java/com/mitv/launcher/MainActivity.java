package com.mitv.launcher;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvApps;
    private AppAdapter appAdapter;
    private List<AppModel> appList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvApps = findViewById(R.id.rvApps);
        rvApps.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        loadInstalledApps();
        appAdapter = new AppAdapter(appList);
        rvApps.setAdapter(appAdapter);
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();

        // ടിവി ആപ്പുകളും ഫോൺ ആപ്പുകളും തിരയുന്നു
        Intent tvIntent = new Intent(Intent.ACTION_MAIN, null);
        tvIntent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> tvActivities = pm.queryIntentActivities(tvIntent, 0);

        Intent standardIntent = new Intent(Intent.ACTION_MAIN, null);
        standardIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> standardActivities = pm.queryIntentActivities(standardIntent, 0);

        List<ResolveInfo> allActivities = new ArrayList<>(tvActivities);
        for (ResolveInfo info : standardActivities) {
            boolean exists = false;
            for (ResolveInfo existing : allActivities) {
                if (existing.activityInfo.packageName.equals(info.activityInfo.packageName)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                allActivities.add(info);
            }
        }

        // സ്വന്തം ആപ്പ് ഒഴിവാക്കി ബാക്കി എല്ലാ ആപ്പുകളും ലിസ്റ്റിലേക്ക് ചേർക്കുന്നു
        for (ResolveInfo ri : allActivities) {
            if (!ri.activityInfo.packageName.equals(getPackageName())) {
                String name = ri.loadLabel(pm).toString();
                Drawable icon = ri.loadIcon(pm);
                String pkg = ri.activityInfo.packageName;
                appList.add(new AppModel(name, icon, pkg));
            }
        }
    }

    public static class AppModel {
        String name;
        Drawable icon;
        String packageName;

        public AppModel(String name, Drawable icon, String packageName) {
            this.name = name;
            this.icon = icon;
            this.packageName = packageName;
        }
    }

    public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
        private List<AppModel> list;

        public AppAdapter(List<AppModel> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            AppModel item = list.get(position);
            holder.txtAppName.setText(item.name);
            holder.imgAppIcon.setImageDrawable(item.icon);

            // റിമോട്ട് ഫോക്കസ് ചെയ്യുമ്പോൾ വലുതാകുന്ന ആനിമേഷൻ (Smooth Focus Scale)
            holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    v.animate().scaleX(1.12f).scaleY(1.12f).setDuration(180).start();
                } else {
                    v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(180).start();
                }
            });

            // റിമോട്ടിൽ OK ബട്ടൺ അമർത്തുമ്പോൾ ആപ്പ് തുറക്കുന്നു
            holder.itemView.setOnClickListener(v -> {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(item.packageName);
                if (launchIntent != null) {
                    startActivity(launchIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgAppIcon;
            TextView txtAppName;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imgAppIcon = itemView.findViewById(R.id.imgAppIcon);
                txtAppName = itemView.findViewById(R.id.txtAppName);
            }
        }
    }
}
