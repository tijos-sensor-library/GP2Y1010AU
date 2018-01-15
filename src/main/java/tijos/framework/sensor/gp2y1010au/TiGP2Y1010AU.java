package tijos.framework.sensor.gp2y1010au;

import java.io.IOException;

import tijos.framework.devicecenter.TiADC;
import tijos.framework.devicecenter.TiGPIO;
import tijos.util.Delay;

/**
 * SHARP GP2Y1010AU0F Dust Density Sensor library for TiJOS  * Based on https://github.com/lixplor/arduino-GP2Y1010AU0F-lib
 * 
 *
 */

public class TiGP2Y1010AU {
	/**
	 * Air quality
	 */
	public enum AQIGrade {
		UNKNOWN, PERFECT, GOOD, POLLUTED_MILD, POLLUTED_MEDIUM, POLLUTED_HEAVY, POLLUTED_SEVERE
	};

	private TiGPIO gpioObj = null;
	private TiADC adcObj = null;
	private int pinId = 0;

	public TiGP2Y1010AU(TiGPIO gpio, int pin, TiADC adc) {
		gpioObj = gpio;
		adcObj = adc;
		this.pinId = pin;
	}

	/**
	 * Initialize GPIO mode
	 * 
	 * @throws IOException
	 */
	public void initialize() throws IOException {
		this.gpioObj.setPinMode(pinId, TiGPIO.MODE_OUTPUT_PP, TiGPIO.MODE_PULL_NONE);
	}

	/**
	 * Get output voltage from ADC
	 * 
	 * @return voltage
	 * @throws IOException
	 */
	public double getOutputV() throws IOException {

		this.gpioObj.writePin(this.pinId, 0);
		Delay.usDelay(280);
		double outputV = this.adcObj.getVoltage();
		Delay.usDelay(40);
		this.gpioObj.writePin(this.pinId, 1);
		Delay.usDelay(9680);

		return outputV;
	}

	/**
	 * Calculate dust density from voltage
	 */
	public double getDustDensity(double outputV) {

		// 0.9~3.4v for 0~500ug/m3
		if (outputV < 0.9) {
			outputV = 0.9;
		} else if (outputV > 3.4) {
			outputV = 3.4;
		}

		// ug/m3 = (V - 0.9) / 5 * 1000
		double ugm3 = (outputV - 0.9) / 5 * 1000;
		return ugm3;
	}

	/**
	 * AQI Calculation
	 * (http://kjs.mep.gov.cn/hjbhbz/bzwb/dqhjbh/jcgfffbz/201203/t20120302_224166.htm
	 */
	public double getAQI(double ugm3) {
		double aqiL = 0;
		double aqiH = 0;
		double bpL = 0;
		double bpH = 0;
		double aqi = 0;

		// Calculate aqi from pm2.5
		if (ugm3 >= 0 && ugm3 <= 35) {
			aqiL = 0;
			aqiH = 50;
			bpL = 0;
			bpH = 35;
		} else if (ugm3 > 35 && ugm3 <= 75) {
			aqiL = 50;
			aqiH = 100;
			bpL = 35;
			bpH = 75;
		} else if (ugm3 > 75 && ugm3 <= 115) {
			aqiL = 100;
			aqiH = 150;
			bpL = 75;
			bpH = 115;
		} else if (ugm3 > 115 && ugm3 <= 150) {
			aqiL = 150;
			aqiH = 200;
			bpL = 115;
			bpH = 150;
		} else if (ugm3 > 150 && ugm3 <= 250) {
			aqiL = 200;
			aqiH = 300;
			bpL = 150;
			bpH = 250;
		} else if (ugm3 > 250 && ugm3 <= 350) {
			aqiL = 300;
			aqiH = 400;
			bpL = 250;
			bpH = 350;
		} else if (ugm3 > 350) {
			aqiL = 400;
			aqiH = 500;
			bpL = 350;
			bpH = 500;
		}
		// aqi = (aqiH - aqiL) / (bpH - bpL) * (desity - bpL) + aqiL;
		aqi = (aqiH - aqiL) / (bpH - bpL) * (ugm3 - bpL) + aqiL;
		return aqi;
	}

	/**
	 * Get Air Quality grade from aqi
	 * 
	 * @param aqi
	 * @return
	 */
	public AQIGrade getGradeInfo(double aqi) {
		if (aqi >= 0 && aqi <= 50) {
			return AQIGrade.PERFECT;
		} else if (aqi > 50 && aqi <= 100) {
			return AQIGrade.GOOD;
		} else if (aqi > 100 && aqi <= 150) {
			return AQIGrade.POLLUTED_MILD;
		} else if (aqi > 150 && aqi <= 200) {
			return AQIGrade.POLLUTED_MEDIUM;
		} else if (aqi > 200 && aqi <= 300) {
			return AQIGrade.POLLUTED_HEAVY;
		} else if (aqi > 300 && aqi <= 500) {
			return AQIGrade.POLLUTED_SEVERE;
		}

		return AQIGrade.UNKNOWN;
	}
}
