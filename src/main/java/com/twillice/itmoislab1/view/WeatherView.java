package com.twillice.itmoislab1.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named @ViewScoped
@Getter @Setter
public class WeatherView implements Serializable {
    private final List<String> cities = List.of(
            "Saint-Petersburg", "Moscow", "London", "New York", "Paris", "Berlin", "Tokyo",
            "Beijing", "Istanbul", "Dubai", "Shanghai", "Rome", "Madrid", "Toronto",
            "Sydney", "Cape Town", "Rio de Janeiro", "Mumbai", "Bangkok", "Singapore",
            "Seoul", "Mexico City", "Cairo", "Los Angeles", "Chicago", "Hong Kong",
            "Barcelona", "Amsterdam", "Vienna", "Stockholm"
    );
    private final int chunkSize = 3;

    private List<Weather> weatherInCities = new ArrayList<>();
    private int lastLoadedCityIndex = -1;

    private OkHttpClient httpClient = new OkHttpClient();
    private ObjectMapper objectMapper = new ObjectMapper();

    public List<Weather> getWeatherInCities() {
        if (lastLoadedCityIndex >= cities.size() - 1)
            return weatherInCities;
        for (int i = lastLoadedCityIndex + 1; i < Math.min(lastLoadedCityIndex + 1 + chunkSize, cities.size()); i++) {
            weatherInCities.add(getWeatherInCity(cities.get(i)));
            lastLoadedCityIndex = i;
        }
        return weatherInCities;
    }

    private Weather getWeatherInCity(String city) {
        Weather weather = new Weather();
        weather.setCity(city);

        Request request = new Request.Builder()
                .url("http://api.weatherapi.com/v1/current.json?key=efcb6cb2f8fe4821b3c134107250403&q=" + city + "&aqi=no")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseData = response.body().string();
            Map<String, Object> weatherData = objectMapper.readValue(responseData, Map.class);
            Map<String, Object> location = (Map<String, Object>) weatherData.get("location");
            Map<String, Object> current = (Map<String, Object>) weatherData.get("current");
            Map<String, Object> condition = (Map<String, Object>) current.get("condition");

            weather.setTemperature(current.get("temp_c") + "°C");
            weather.setWind(current.get("wind_dir") + " " + current.get("wind_kph") + " km/h");
            weather.setDescription((String) condition.get("text"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return weather;
    }

    @Getter @Setter
    public static class Weather {
        private String city;
        private String temperature;
        private String wind;
        private String description;
    }
}
