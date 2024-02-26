package com.weatherappgui;

import javax.swing.SwingUtilities;

public class App {

	public static void main(String[] args){
		SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				new WeatherAppGui().setVisible(true);

				//System.out.println(WeatherApp.getLocationData("Manila"));
				//System.out.println(WeatherApp.getCurrentTime());
				//System.out.println(WeatherApp.getWeatherData("Manila"));

			}
		});
	}
}
