package com.ozanyazici.javamaps;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.ozanyazici.javamaps.databinding.ActivityMapsBinding;
import com.ozanyazici.javamaps.model.Place;
import com.ozanyazici.javamaps.roomdb.PlaceDao;
import com.ozanyazici.javamaps.roomdb.PlaceDatabase;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    //İzin istemek için launcher tanımladım.
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    PlaceDatabase db;
    PlaceDao placeDao;
    Double selectedLatitude;
    Double selectedLongitude;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    Place selectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        registerLauncher();

        sharedPreferences = this.getSharedPreferences("com.ozanyazici.javamaps",MODE_PRIVATE);
        info = false;

        db = Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();
        placeDao = db.placeDao();

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;

        binding.saveButton.setEnabled(false);
    }


    //Harita hazır old. yapılacak işler bu metotda
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this); //Uyguladığım arayüzü bu haritada kullanacağımı belirtiyorum.

        Intent intent = getIntent();
        String intentInfo = intent.getStringExtra("info");

        if(intentInfo.equals("new")) { //Yeni Yer eklenecekse

            binding.saveButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setVisibility(View.GONE);
            //Gone nin Invısıble dan farkı buton gidicek ve yerine diğer görünümler gelebilecek. Harita aşağı doğru genişlesin diye bunu yapıyoruz.

            //LocationManager Sistemin konum servislerine erişimini sağlıyor.
            //Herhangi bir servis seçip herhangi birşey geri döndürebileceği için bizden object istiyor. casting yapıyoruz.
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            //KOnum değişikliklerini dinler
            locationListener = new LocationListener() {
                //Konum değiştiğinde yapılacakların metodu
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    info = sharedPreferences.getBoolean("info",false);

                    if(!info) {
                        //Haritada özgürce hareket etmek için bu kısmın bir kere çalışması lazım onon için sharedp. ile bir kere çalışacak şekilde ayarlıyorum.
                        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                        sharedPreferences.edit().putBoolean("info",true).apply();
                    }

                }
            };

            //Permission Control
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Snackbar.make(binding.getRoot(),"Permission needed for maps",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);

                        }
                    }).show();

                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            } else {
                //Konum bilgisi providerını, ne kadar süre ve mesafede güncelleneceğini belirttik bu parametrrelerle konum değişikliğini alıyoruz.
                //0,0 olunca sürekli konum bilgisini güncelliyor.
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

                //Son bilinen konum başlangıçta kamerayı oraya odaklıyoruz.
                Location lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastLocation != null) {
                    LatLng lastUserlocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserlocation,15));
                }
                //olduğum yere mavi nokya ekliyor.
                mMap.setMyLocationEnabled(true);
            }
            
        } else { //Kayıtlı olan yer görüntülenecekse

            mMap.clear();

            mMap.setMyLocationEnabled(true);

            selectedPlace = (Place) intent.getSerializableExtra("place");

            LatLng latLng = new LatLng(selectedPlace.latitude,selectedPlace.longitude);

            mMap.addMarker(new MarkerOptions().position(latLng).title(selectedPlace.name));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

            binding.placeNameText.setText(selectedPlace.name);
            binding.saveButton.setVisibility(View.GONE);
            binding.deleteButton.setVisibility(View.VISIBLE);

        }

    }

    private void registerLauncher() {

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result) {
                    if (ContextCompat.checkSelfPermission(MapsActivity.this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    }
                } else {
                    Toast.makeText(MapsActivity.this, "Permission needed!", Toast.LENGTH_SHORT).show();
                }

            }

        });

    }

    //Haritada bir yere uzun tıklandığında çalışıyor. Parametre olarak tıklanan yerin enlemini boylamını alıyor.
    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {

        //Aynı anda sadece bir marker olmasını istediğim için biri eklendiğinde diğerini siliyorum.
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLongitude = latLng.longitude;
        selectedLatitude = latLng.latitude;

        binding.saveButton.setEnabled(true);

    }

    public void save(View view){

        Place place = new Place(binding.placeNameText.getText().toString(),selectedLatitude,selectedLongitude);

        //threading -> Main (UI), Default (CPU Intensive), IO (network, database)

        //placeDao.insert(place).subscribeOn(Schedulers.io()).subscribe(); böylede yapabilirdik ama disposable daha verimli.

        //disposable tek kullanımlık anlamında
        compositeDisposable.add(placeDao.insert(place)
                .subscribeOn(Schedulers.io()) //Hangi threadde bu işlemin yapılacağı.
                .observeOn(AndroidSchedulers.mainThread()) //Hangi threadde gözlemleneceği.
                .subscribe(MapsActivity.this::handleResponse) //Başlatmak için. Ve işlem bittikten sonra handleResponse u çalıştıracak ve mainActiviteye geri dönecek.
        );
    }

    private void handleResponse() {
        Intent intent = new Intent(MapsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void delete(View view) {

        if (selectedPlace != null) { //Ekstra kontrol, etmesekte olur
            compositeDisposable.add(placeDao.delete(selectedPlace)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(MapsActivity.this::handleResponse)
            );
        }

    }

    //Observeablelar flowablelar falan bir disposable ın içine konabiliyor. Daha sonra aktivite kapandığında hepsi silinebiliyor. Ve hafızada yer tutmuyor.
    //Disposable ı tek kullanımlık poşet gibi düşünebiliriz.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}