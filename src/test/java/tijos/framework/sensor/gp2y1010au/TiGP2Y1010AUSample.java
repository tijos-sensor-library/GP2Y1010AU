package tijos.framework.sensor.gp2y1010au;

import java.io.IOException;

import tijos.framework.devicecenter.TiADC;
import tijos.framework.devicecenter.TiGPIO;
import tijos.framework.sensor.gp2y1010au.TiGP2Y1010AU;
import tijos.util.Delay;

public class TiGP2Y1010AUSample {

	public static void main(String[] args) {

		try {

			//GPIO port ID 
			int adcPort0 = 0;
			
			//TiADC port id
			int gpioPort0 = 0;

			//GPIO PIN ID of the port
			int gpioPin0 = 0;
			
			/*
			 * 资源分配， 将gpioPort与gpioPin0分配给TiGPIO对象gpio0 将adcPort0分配给TiADC对象adc0
			 */
			TiGPIO gpio0 = TiGPIO.open(gpioPort0, gpioPin0);
			TiADC adc0 = TiADC.open(adcPort0);
			/*
			 * 资源绑定， 创建TiGeneralSensor对象并将gpioPort、gpioPortPin和adcPort与其绑定
			 * Pin0<---->D0 ADC <---->A0
			 */
			TiGP2Y1010AU gp2y1014au = new TiGP2Y1010AU(gpio0, gpioPin0, adc0);
			gp2y1014au.initialize();

			while (true) {
				try {

					double outputV = gp2y1014au.getOutputV(); 
					double ugm3 = gp2y1014au.getDustDensity(outputV); // Dust density
					double aqi = gp2y1014au.getAQI(ugm3); // aqi
					TiGP2Y1010AU.AQIGrade gradeInfo = gp2y1014au.getGradeInfo(aqi);

					 System.out.println("outputV=" + outputV + "\tug/m3=" + ugm3 + "\tAQI=" + aqi);
					 System.out.println("\tgrade=" + gradeInfo);
					 
					 Delay.msDelay(1000);
				} catch (IOException ie) {

					ie.printStackTrace();
				}
			}

		} catch (IOException ie) {

			ie.printStackTrace();
		}
	}
}
