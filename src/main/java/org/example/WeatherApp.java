package org.example; // Пакет для вашего класса
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
class WeatherData {
    private String cityName;
    private double temperature;
    private double feelsLike;
    private double tempMin;
    private double tempMax;
    private double humidity;
    private double pressure;
    private double windSpeed;
    private String windDirection;
    private String weatherDescription;
    private String icon;

    // Геттеры и сеттеры
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(double feelsLike) {
        this.feelsLike = feelsLike;
    }

    public double getTempMin() {
        return tempMin;
    }

    public void setTempMin(double tempMin) {
        this.tempMin = tempMin;
    }

    public double getTempMax() {
        return tempMax;
    }

    public void setTempMax(double tempMax) {
        this.tempMax = tempMax;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
public class WeatherApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(WeatherApp.class);
    public static final String API_KEY = "a38791126f2053798865dcb2abeb1800";

    private TextField searchField;
    private ListView<String> suggestionsList;
    private Label cityLabel;
    private Label temperatureLabel;
    private Label windLabel;
    private Label humidityLabel;
    private ImageView weatherIcon;
    private VBox forecastBox;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Weather App");

        // UI elements
        searchField = new TextField();
        searchField.setPromptText("Введите название города");

        suggestionsList = new ListView<>();
        suggestionsList.setMaxHeight(100);
        suggestionsList.setVisible(false);

        cityLabel = new Label("Город: ");
        temperatureLabel = new Label("Температура: ");
        windLabel = new Label("Ветер: ");
        humidityLabel = new Label("Влажность: ");
        weatherIcon = new ImageView();

        forecastBox = new VBox(5);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(searchField, suggestionsList, cityLabel, temperatureLabel, windLabel, humidityLabel, weatherIcon, new Label("Прогноз на 4 дня:"), forecastBox);

        // Обработчик для поиска города
        searchField.setOnKeyTyped(e -> {
            String query = searchField.getText();
            if (!query.isEmpty()) {
                try {
                    ObservableList<String> suggestions = FXCollections.observableArrayList(getCitySuggestions(query));
                    suggestionsList.setItems(suggestions);
                    suggestionsList.setVisible(true);
                } catch (Exception ex) {
                    logger.error("Ошибка при получении подсказок: {}", ex.getMessage());
                }
            } else {
                suggestionsList.setVisible(false);
            }
        });

        searchField.setOnAction(e -> loadWeather(searchField.getText()));
        suggestionsList.setOnMouseClicked(e -> {
            String selectedCity = suggestionsList.getSelectionModel().getSelectedItem();
            if (selectedCity != null) {
                loadWeather(selectedCity);
            }
        });

        Scene scene = new Scene(layout, 800, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadWeather(String city) {
        try {
            WeatherData weatherData = WeatherAPIClient.getCurrentWeather(city);
            updateWeatherDisplay(weatherData);

            JSONArray forecastData = WeatherAPIClient.getForecast(city);
            updateForecastDisplay(forecastData);

            logger.info("Погода для города {} успешно загружена", city);
            suggestionsList.setVisible(false);
        } catch (Exception ex) {
            showErrorDialog("Город не найден!");
            logger.error("Ошибка при запросе погоды: {}", ex.getMessage());
        }
    }

    private void updateWeatherDisplay(WeatherData weatherData) {
        cityLabel.setText("Город: " + weatherData.getCityName());
        temperatureLabel.setText("Температура: " + weatherData.getTemperature() + "°C");
        windLabel.setText("Ветер: " + weatherData.getWindSpeed() + " м/с, " + weatherData.getWindDirection());
        humidityLabel.setText("Влажность: " + weatherData.getHumidity() + "%");

        String iconUrl = "http://openweathermap.org/img/wn/" + weatherData.getIcon() + "@2x.png";
        weatherIcon.setImage(new Image(iconUrl));
    }

    private void updateForecastDisplay(JSONArray forecastData) {
        forecastBox.getChildren().clear();
        for (int i = 0; i < 4; i++) {
            JSONObject dayForecast = forecastData.getJSONObject(i);

            String date = dayForecast.getString("date");
            double avgTemp = dayForecast.getDouble("avgTemp");
            double minTemp = dayForecast.getDouble("minTemp");
            double maxTemp = dayForecast.getDouble("maxTemp");
            String status = dayForecast.getString("status");
            String icon = dayForecast.getString("icon");

            HBox dayBox = new HBox(10);
            dayBox.getChildren().addAll(
                    new ImageView(new Image("http://openweathermap.org/img/wn/" + icon + "@2x.png")),
                    new Label(String.format("%s: %s, Средняя: %.1f°C, Мин: %.1f°C, Макс: %.1f°C", date, status, avgTemp, minTemp, maxTemp))
            );

            forecastBox.getChildren().add(dayBox);
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private ObservableList<String> getCitySuggestions(String query) throws Exception {
        // Реализовать логику получения подсказок (например, через API OpenWeatherMap)
        return FXCollections.observableArrayList("Москва", "Минск", "Мурманск", "Мадрид", "Милан"); // Заглушка
    }
}

class WeatherAPIClient {
    public static WeatherData getCurrentWeather(String city) throws Exception {
        String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + WeatherApp.API_KEY + "&units=metric";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return parseWeatherData(response.toString());
    }

    public static JSONArray getForecast(String city) throws Exception {
        String urlString = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + WeatherApp.API_KEY + "&units=metric";
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray list = jsonResponse.getJSONArray("list");

        JSONArray result = new JSONArray();
        for (int i = 0; i < list.length(); i += 8) { // Каждые 8 записей - один день
            JSONObject dayData = list.getJSONObject(i);
            JSONObject main = dayData.getJSONObject("main");
            JSONArray weatherArray = dayData.getJSONArray("weather");

            JSONObject dayForecast = new JSONObject();
            dayForecast.put("date", dayData.getString("dt_txt"));
            dayForecast.put("avgTemp", main.getDouble("temp"));
            dayForecast.put("minTemp", main.getDouble("temp_min"));
            dayForecast.put("maxTemp", main.getDouble("temp_max"));
            dayForecast.put("status", weatherArray.getJSONObject(0).getString("description"));
            dayForecast.put("icon", weatherArray.getJSONObject(0).getString("icon"));

            result.put(dayForecast);
        }

        return result;
    }

    private static WeatherData parseWeatherData(String response) {
        JSONObject jsonResponse = new JSONObject(response);
        WeatherData data = new WeatherData();
        data.setCityName(jsonResponse.getString("name"));
        data.setTemperature(jsonResponse.getJSONObject("main").getDouble("temp"));
        data.setFeelsLike(jsonResponse.getJSONObject("main").getDouble("feels_like"));
        data.setTempMin(jsonResponse.getJSONObject("main").getDouble("temp_min"));
        data.setTempMax(jsonResponse.getJSONObject("main").getDouble("temp_max"));
        data.setHumidity(jsonResponse.getJSONObject("main").getDouble("humidity"));
        data.setPressure(jsonResponse.getJSONObject("main").getDouble("pressure"));
        data.setWindSpeed(jsonResponse.getJSONObject("wind").getDouble("speed"));

        double windDeg = jsonResponse.getJSONObject("wind").getDouble("deg");
        if (windDeg >= 0 && windDeg < 90) {
            data.setWindDirection("Северо-восточный");
        } else if (windDeg >= 90 && windDeg < 180) {
            data.setWindDirection("Юго-восточный");
        } else if (windDeg >= 180 && windDeg < 270) {
            data.setWindDirection("Юго-западный");
        } else {
            data.setWindDirection("Северо-западный");
        }

        JSONArray weatherArray = jsonResponse.getJSONArray("weather");
        data.setWeatherDescription(weatherArray.getJSONObject(0).getString("description"));
        data.setIcon(weatherArray.getJSONObject(0).getString("icon"));

        return data;
    }
}
