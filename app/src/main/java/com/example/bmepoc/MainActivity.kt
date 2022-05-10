package com.example.bmepoc

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit
import java.lang.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bme = BME68x(this)
        bme.operatingMode = BME68x.OperatingMode.SLEEP
        println(bme.operatingMode)

        bme.softReset()

        for (x in 0..1) {
            forcedModeTest(bme)
            bme.softReset()
        }
    }

    private fun forcedModeTest(bme: BME68x) {
        System.out.format(
            "hum os: %s, temp os: %s, press os: %s, IIR Filter: %s, ODR: %s%n",
            bme.humidityOversample, bme.temperatureOversample, bme.pressureOversample,
            bme.iirFilterConfig, bme.odr
        )
        bme.setConfiguration(
            BME68x.OversamplingMultiplier.X2, BME68x.OversamplingMultiplier.X2, BME68x.OversamplingMultiplier.X2,
            BME68x.IirFilterCoefficient._3, BME68x.ODR.NONE
        )
        System.out.format(
            "hum os: %s, temp os: %s, press os: %s, IIR Filter: %s, ODR: %s%n",
            bme.humidityOversample, bme.temperatureOversample, bme.pressureOversample,
            bme.iirFilterConfig, bme.odr
        )
        Log.d("main", "set heater")
        val targetOperatingMode = BME68x.OperatingMode.FORCED
        bme.setHeaterConfiguration(targetOperatingMode, BME68x.HeaterConfig(true, 320, 150))

        Log.d("main", "getting duration")
        // Calculate delay period in microseconds
        val measureDurationMs =
            (bme.calculateMeasureDuration(targetOperatingMode) / 1000).toLong()
        // System.out.println("measure_duration_ms: " + measure_duration_ms + "
        // milliseconds");

        Log.d("main", "sleep")
        TimeUnit.MILLISECONDS.sleep(measureDurationMs)

        for (i in 0..4) {
            for ((reading, data) in bme.getSensorData(targetOperatingMode).withIndex()) {
                System.out.format(
                    "Reading [%d]: Idx: %,d. Temperature: %,.2f C. Pressure: %,.2f hPa. Relative Humidity: %,.2f %%rH. Gas Idx: %,d. Gas Resistance: %,.2f Ohms. IDAC: %,.2f mA. Gas Wait: %,d (ms or multiplier). (heater stable: %b, gas valid: %b).%n",
                    Integer.valueOf(reading),
                    Integer.valueOf(data.measureIndex),
                    java.lang.Float.valueOf(data.temperature),
                    java.lang.Float.valueOf(data.pressure),
                    java.lang.Float.valueOf(data.humidity),
                    Integer.valueOf(data.gasMeasurementIndex),
                    java.lang.Float.valueOf(data.gasResistance),
                    java.lang.Float.valueOf(data.idacHeatMA),
                    java.lang.Short.valueOf(data.gasWait),
                    java.lang.Boolean.valueOf(data.isHeaterTempStable),
                    java.lang.Boolean.valueOf(data.isGasMeasurementValid)
                )
            }

            TimeUnit.SECONDS.sleep(1)
        }
    }
}