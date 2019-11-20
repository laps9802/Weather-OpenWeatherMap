package com.joseph.weatheropenweathermap


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_open_weather.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OpenWeatherActivity : AppCompatActivity(), LocationListener {

    private val PERMISSION_REQUEST_CODE = 2000
    private val APP_ID = "*************"        //sensitive information deleted
    private val UNITS = "metric"
    private val LANGUAGE = "kr"
    private lateinit var backPressHolder: OnBackPressHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_weather)

        backPressHolder = OnBackPressHolder()
        getLocationInfo()

        setting.setOnClickListener {
            startActivity(Intent(this, AccountSettingActivity::class.java))
//            requestCurrentWeather()
        }

    }

    override fun onBackPressed() {
        backPressHolder.onBackPressed()
    }

    private fun drawCurrentWeather(currentWeather: TotalWeather) {
        with(currentWeather) {      // with 안에서는 해당 클래스 맥락 -> this == TotalWeather 클래스 의미

            this.weatherList?.getOrNull(0)?.let {
                it.icon?.let {
                    val glide = Glide.with(this@OpenWeatherActivity)        // OpenWeather~ 클래스를 사용하려면 @{클래스명} 해줘야함
                    glide
                        .load(Uri.parse("https://openweathermap.org/img/w/" + it + ".png"))
                        .into(current_icon)
                }
                it.main?.let { current_main.text = it }
                it.description?.let { current_description.text = it }
            }

            this.main?.temp?.let { current_now.text = String.format("%.1f", it) }
            this.main?.tempMax?.let { current_max.text = String.format("%.1f", it) }
            this.main?.tempMin?.let { current_min.text = String.format("%.1f", it) }

            loading_view.visibility = View.GONE
            weather_view.visibility = View.VISIBLE

        }

    }

    //위치정보 - 메인메소드
    private fun getLocationInfo() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(        //*권한 여부 - onActivityResult
                this@OpenWeatherActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@OpenWeatherActivity,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
        } else {
            val locationManager =
                this.getSystemService(Context.LOCATION_SERVICE) as LocationManager        // Context 소속 로케이션 객체 생성
            val location =
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)           //가장 최근에 사용된 위치정보를 얻어냄
            if (location != null) {                                                  //*위치 정보 여부
                val latitude = location.latitude
                val longitude = location.longitude
                requestWeatherInfoOfLocation(latitude = latitude, longitude = longitude)
            } else {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,            // NETOWRK_PROVIDER : 위치정보-통신 / GPS_PROVIDER : 위치정보-GPS -> 네트워크가 좀더 정확
                    0L,
                    0F,
                    this
                )                                               //update 결과는 onLocationChanged
                locationManager.removeUpdates(this)
            }
        }

    }

    //권한 요청 결과
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) getLocationInfo()          //권한이 있기 때문에 로케이션 얻기 로직으로 이동
        }
    }

    //위치정보 업데이트 되었을 시
    override fun onLocationChanged(location: Location?) {
        var latitude = location?.latitude
        var longitude = location?.longitude
        if (latitude != null && longitude != null)
            requestWeatherInfoOfLocation(latitude = latitude, longitude = longitude)
    }


    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }


    // API Call - Location
    private fun requestWeatherInfoOfLocation(latitude: Double, longitude: Double) {
        (application as WeatherApplication)
            .requestService()
            ?.getWeatherInfoOfCoordinates(
                latitude = latitude,
                longitude = longitude,
                appID = APP_ID,
                units = UNITS,
                language = LANGUAGE
            )
            ?.enqueue(object : Callback<TotalWeather> {
                override fun onFailure(call: Call<TotalWeather>, t: Throwable) {
                    loading_text.text = "로딩 실패"
                }

                override fun onResponse(call: Call<TotalWeather>, response: Response<TotalWeather>) {
                    if (response.isSuccessful) {
                        val totalWeather = response.body()
                        totalWeather?.let { drawCurrentWeather(it) }
                    } else{
                        loading_text.text = "로딩 실패"
                    }

                }
            })
    }


    inner class OnBackPressHolder() {
        private var backPressHolder: Long = 0

        fun onBackPressed() {
            if (System.currentTimeMillis() > backPressHolder + 2000) {
                backPressHolder = System.currentTimeMillis()            //처음 백버튼 누를때 시간저장
                showBackToast()
                return
            }
            if (System.currentTimeMillis() <= backPressHolder + 2000) {   //처음 누르고 2초안에 더 누를 시 종료
                finishAffinity()                // 백그라운드로 나가짐(정책 상)
            }
        }

        fun showBackToast() {
            Toast.makeText(this@OpenWeatherActivity, "한번 더 누르면 종료됩니다", Toast.LENGTH_SHORT).show()
        }

    }


}
