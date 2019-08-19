package com.rsmartin.fuelapp.presentation.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.rsmartin.fuelapp.App;
import com.rsmartin.fuelapp.IExtras;
import com.rsmartin.fuelapp.R;
import com.rsmartin.fuelapp.domain.model.DatosGasolinera;
import com.rsmartin.fuelapp.domain.model.ListaDatosGasolineras;
import com.rsmartin.fuelapp.presentation.internal.android.SharedPref;
import com.rsmartin.fuelapp.presentation.room.database.AppDB;
import com.rsmartin.fuelapp.presentation.ui.AbstractActivity;
import com.rsmartin.fuelapp.presentation.ui.map.MapsActivity;
import com.rsmartin.fuelapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AbstractActivity implements SplashPresenter.View {

    private final String TAG = "SplashActivity";

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastLocation;
    private LatLng currentLatLon;

    @Inject
    SplashPresenter splashPresenter;

    @BindView(R.id.response)
    TextView tvResponse;

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_view);
        ButterKnife.bind(this);

        getApplicationComponent().inject(this);
        splashPresenter.setView(this);

        initLocation();

        if (!SharedPref.getInstance().getBooleanPreferences(IExtras.IS_NOT_FIRST_TIME)) {
            Log.e(TAG, "onCreate: Primera vez // Llama Retrofit");
            SharedPref.getInstance().saveBooleanPreferences(IExtras.IS_NOT_FIRST_TIME, true);
            splashPresenter.getOils();
        } else {
            Log.e(TAG, "onCreate: NO Primera vez // Llama Room");
            FindAllListaPrecioWraperTask findAllListaPrecioWraperTask = new FindAllListaPrecioWraperTask();
            findAllListaPrecioWraperTask.execute();
        }

    }

    @SuppressLint("MissingPermission")
    private void initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    lastLocation = location;
                    currentLatLon = new LatLng(location.getLatitude(), location.getLongitude());
                }
            }
        });
    }

    @Override
    public void showResult(ListaDatosGasolineras listaDatosGasolineras) {
        InsertListaPrecioWraperTask insertListaPrecioWraperTask = new InsertListaPrecioWraperTask();
        insertListaPrecioWraperTask.execute(listaDatosGasolineras.getDatosGasolineraList());

        showResultFromRoom(listaDatosGasolineras);
    }

    public void showResultFromRoom(ListaDatosGasolineras listaDatosGasolineras) {

        List<DatosGasolinera> lista = new ArrayList<>();
        boolean isDistanceMinima = false;

        for (DatosGasolinera item : listaDatosGasolineras.getDatosGasolineraList()) {

            if (item.getLat() != null && item.getLon() != null) {
                isDistanceMinima = Utils.distance(currentLatLon.latitude, currentLatLon.longitude,
                        Utils.replaceComaToDot(item.getLat()), Utils.replaceComaToDot(item.getLon()))
                        <= 100;
                if (isDistanceMinima) {
                    lista.add(item);
                }
            }
        }

        ListaDatosGasolineras listaDatosGasolinerasShort = new ListaDatosGasolineras(lista);

        tvResponse.setText("finalizado");

        Intent i = new Intent(this, MapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("value", listaDatosGasolinerasShort);
        i.putExtras(bundle);
        startActivity(i);
    }

    public class FindAllListaPrecioWraperTask extends AsyncTask<Void, Void, List<DatosGasolinera>> {

        @Override
        protected List<DatosGasolinera> doInBackground(Void... voids) {
            return AppDB.getInstance(App.getInstance().getApplicationContext())
                    .gasolinerasDAO().findAllPreciosGasolineras();
        }

        @Override
        protected void onPostExecute(List<DatosGasolinera> lists) {
            showResultFromRoom(new ListaDatosGasolineras(lists));
        }
    }

    public class InsertListaPrecioWraperTask extends AsyncTask<List<DatosGasolinera>, Void, Void> {
        @Override
        protected Void doInBackground(List<DatosGasolinera>... lists) {

            for (DatosGasolinera item : lists[0]) {
                AppDB.getInstance(App.getInstance().getApplicationContext())
                        .gasolinerasDAO().insertPrecioGasolinera(item);
            }
            return null;
        }
    }

    @Override
    public void showLoader() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoader() {
        progressBar.setVisibility(View.INVISIBLE);
    }

}
