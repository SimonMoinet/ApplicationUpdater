package com.dayries.applicationupdater;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.android.volley.BuildConfig;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.dayries.applicationupdater.Model.Package;
import com.dayries.applicationupdater.Request.GsonRequest;
import com.dayries.applicationupdater.Request.InputStreamRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public class ApplicationUpdater {

    private Context context;

    private String url;

    private String onUpdateTitle;
    private String onUpdateMessage;
    private String onUpdateBtnUpdate;
    private String onUpdateBtnDismiss;

    /**
     * Constructeur de la classe ApplicationUpdater
     * @param context : context de l'application
     */
    public ApplicationUpdater(Context context, String url) {
        this.context = context;
        this.url = url;

        this.onUpdateTitle = context.getString(R.string.onUpdateTitle);
        this.onUpdateMessage = context.getString(R.string.onUpdateMessage);
        this.onUpdateBtnUpdate = context.getString(R.string.onUpdateBtnUpdate);
        this.onUpdateBtnDismiss = context.getString(R.string.onUpdateBtnDismiss);
    }

    // ----------------------------------------------------------------
    //
    // ----------------------------------------------------------------

    public ApplicationUpdater start()
    {
        if(!VolleyConnection.getInstance(context.getApplicationContext()).isNetworkAvailable()) {
            error("Erreur", "Aucune connection à internet n'est disponible pour le moment");
            return this;
        }

        if(!isUrlValid()) {
            error("Erreur", "L'url qui pointe vers le fichier json de version n'est pas valide.");
            return this;
        }

        GsonRequest gsonRequest = new GsonRequest<Package>(this.url, Package.class, null, new Response.Listener<Package>() {
            @Override
            public void onResponse(Package latest) {
                if (isUpdateAvailable(latest, getCurrentPackage())) {
                    dialogUpdate(latest);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error("Volley", error.toString());
            }
        });

        VolleyConnection.getInstance(this.context.getApplicationContext()).addToRequestQueue(gsonRequest);

        return this;
    }

    public void update(Package latest) {
        // TODO : verification de l'url

        InputStreamRequest request = new InputStreamRequest(Request.Method.GET, latest.getUrl(), null,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        // TODO handle the response
                        try {
                            if (response!=null) {

                                File sdcard = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                                File file = new File(sdcard, "entrepot.apk");

                                FileOutputStream fileOutput = new FileOutputStream(file);
                                fileOutput.write(response);
                                fileOutput.close();


                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(UtilsLibrairy.uriFromFile(context, new File(file.getPath())), "application/vnd.android.package-archive");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                context.startActivity(intent);
                            }
                        } catch (Exception e) {
                            error("Erreur", "Impossible de télécharger le fichier apk.\n" + e.toString());
                            e.printStackTrace();
                        }
                    }
                } ,new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error("Volley", error.toString());
                error.printStackTrace();
            }
        });

        VolleyConnection.getInstance(context).addToRequestQueue(request);
    }

    private Package getCurrentPackage() {

        Package current = new Package();

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            current.setVersion(version);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return current;
    }

    // ----------------------------------------------------------------
    // Verification des permissions
    // ----------------------------------------------------------------

    public Boolean hasNeededPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();

        if(ContextCompat.checkSelfPermission(this.context, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if(ContextCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_NETWORK_STATE);
        }
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this.context, Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.REQUEST_INSTALL_PACKAGES);
            }
        }

        if(!listPermissionsNeeded.isEmpty()) {
            String message = "";

            for (int i=0; i < listPermissionsNeeded.size(); i++) {
                message += listPermissionsNeeded.get(i) + "\n";
            }

            error("Des permissions sont manquantes", message);
            return false;
        }

        return true;
    }

    private boolean isUrlValid() {
        if(this.url.matches("")) {
            return false;
        } else if(!this.url.contains(".")) {
            return false;
        } else if (!this.url.substring(this.url.lastIndexOf(".") + 1).matches("json")) {
            return false;
        }

        return true;
    }

    private boolean isUpdateAvailable(Package latest, Package current) {
        if(latest.getVersion().compareTo(current.getVersion()) > 0) {
            return true;
        }

        return false;
    }



    // ----------------------------------------------------------------
    //
    // ----------------------------------------------------------------

    private void error(String titre, String message) {
        if(BuildConfig.DEBUG) {
            new AlertDialog.Builder(this.context.getApplicationContext())
                    .setTitle(titre)
                    .setMessage(message)
                    .show();
        } else {
            Log.e(ApplicationUpdater.class.toString(), titre + " : " + message);
        }
    }

    public void dialogUpdate(final Package latest) {

        if(!latest.getReleaseNotes().isEmpty()) {
            if(this.onUpdateMessage.matches("")) {
                this.onUpdateMessage = "Notes de mise à jour :\n";
            } else {
                this.onUpdateMessage += "\nNotes de mise à jour :\n";
            }

            for (int i = 0; i < latest.getReleaseNotes().size(); i++) {
                this.onUpdateMessage += latest.getReleaseNotes().get(i) + "\n";
            }
        }

        new AlertDialog.Builder(context)
                .setTitle(this.onUpdateTitle)
                .setMessage(onUpdateMessage)
                .setPositiveButton(this.onUpdateBtnUpdate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        update(latest);
                    }
                })
                .setNegativeButton(this.onUpdateBtnDismiss, null)
                .setIcon(android.R.drawable.stat_sys_warning)
                .show();
    }
}
