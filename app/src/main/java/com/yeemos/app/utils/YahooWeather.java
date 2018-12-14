package com.yeemos.app.utils;

import android.content.Context;
import android.net.Uri;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by gigabud on 16-8-4.
 */
public class YahooWeather {

    private static final String YQL_WEATHER_ENDPOINT_AUTHORITY = "query.yahooapis.com";
    private static final String YQL_WEATHER_ENDPOINT_PATH = "/v1/public/yql";
    public static final String YAHOO_WEATHER_ERROR = "Yahoo! Weather - Error";
    public static final int FORECAST_INFO_MAX_SIZE = 5;

    public WeatherInfo getWeatherString(Context context, String placeName) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https");
        builder.authority(YQL_WEATHER_ENDPOINT_AUTHORITY);
        builder.path(YQL_WEATHER_ENDPOINT_PATH);
        builder.appendQueryParameter("q", "select * from weather.forecast where woeid in" +
                "(select woeid from geo.places(1) where text=\"" +
                placeName +
                "\") and u = 'c'");
        String queryUrl = builder.build().toString();

        URL url = null;
        InputStream response = null;
        HttpURLConnection connection = null;
        try {
            url = new URL(queryUrl);
            connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setConnectTimeout(20 * 1000);
            connection.setReadTimeout(20 * 1000);
            connection.setDefaultUseCaches(true);
            response = connection.getInputStream();
            int status = connection.getResponseCode();
            if (status / 100 == 2) {
                return parseWeatherInfo(context, convertStringToDocument(context, convertStreamToString(response)));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private Document convertStringToDocument(Context context, String src) {
        Document dest = null;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser;

        try {
            parser = dbFactory.newDocumentBuilder();
            dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dest;
    }

    /**
     * Convert inputstrem to string as output
     */
    private String convertStreamToString(InputStream is) throws IOException {

        String out = null;

        if (is != null) {
            Writer writer = new StringWriter(4096);
            Reader reader = null;
            char[] buffer = new char[4096];
            try {
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                out = writer.toString();
                return out;

            } finally {
                if (is != null)
                    is.close();
                if (writer != null)
                    writer.close();
                if (reader != null)
                    reader.close();
            }

        } else {
            return "";
        }
    }

    private WeatherInfo parseWeatherInfo(Context context, Document doc) {
        WeatherInfo weatherInfo = new WeatherInfo();
        try {

            Node titleNode = doc.getElementsByTagName("title").item(0);

            if (titleNode.getTextContent().equals(YAHOO_WEATHER_ERROR)) {
                return null;
            }

            weatherInfo.setTitle(titleNode.getTextContent());
            weatherInfo.setDescription(doc.getElementsByTagName("description").item(0).getTextContent());
            weatherInfo.setLanguage(doc.getElementsByTagName("language").item(0).getTextContent());
            weatherInfo.setLastBuildDate(doc.getElementsByTagName("lastBuildDate").item(0).getTextContent());

            Node locationNode = doc.getElementsByTagName("yweather:location").item(0);
            weatherInfo.setLocationCity(locationNode.getAttributes().getNamedItem("city").getNodeValue());
            weatherInfo.setLocationRegion(locationNode.getAttributes().getNamedItem("region").getNodeValue());
            weatherInfo.setLocationCountry(locationNode.getAttributes().getNamedItem("country").getNodeValue());

            Node windNode = doc.getElementsByTagName("yweather:wind").item(0);
            weatherInfo.setWindChill(windNode.getAttributes().getNamedItem("chill").getNodeValue());
            weatherInfo.setWindDirection(windNode.getAttributes().getNamedItem("direction").getNodeValue());
            weatherInfo.setWindSpeed(windNode.getAttributes().getNamedItem("speed").getNodeValue());

            Node atmosphereNode = doc.getElementsByTagName("yweather:atmosphere").item(0);
            weatherInfo.setAtmosphereHumidity(atmosphereNode.getAttributes().getNamedItem("humidity").getNodeValue());
            weatherInfo.setAtmosphereVisibility(atmosphereNode.getAttributes().getNamedItem("visibility").getNodeValue());
            weatherInfo.setAtmospherePressure(atmosphereNode.getAttributes().getNamedItem("pressure").getNodeValue());
            weatherInfo.setAtmosphereRising(atmosphereNode.getAttributes().getNamedItem("rising").getNodeValue());

            Node astronomyNode = doc.getElementsByTagName("yweather:astronomy").item(0);
            weatherInfo.setAstronomySunrise(astronomyNode.getAttributes().getNamedItem("sunrise").getNodeValue());
            weatherInfo.setAstronomySunset(astronomyNode.getAttributes().getNamedItem("sunset").getNodeValue());

            weatherInfo.setConditionTitle(doc.getElementsByTagName("title").item(2).getTextContent());
            weatherInfo.setConditionLat(doc.getElementsByTagName("geo:lat").item(0).getTextContent());
            weatherInfo.setConditionLon(doc.getElementsByTagName("geo:long").item(0).getTextContent());

            Node currentConditionNode = doc.getElementsByTagName("yweather:condition").item(0);
            weatherInfo.setCurrentCode(
                    Integer.parseInt(
                            currentConditionNode.getAttributes().getNamedItem("code").getNodeValue()
                    ));
            weatherInfo.setCurrentText(
                    currentConditionNode.getAttributes().getNamedItem("text").getNodeValue());
            int curTempC = Integer.parseInt(currentConditionNode.getAttributes().getNamedItem("temp").getNodeValue());
            weatherInfo.setCurrentTemp(curTempC);
            weatherInfo.setCurrentConditionDate(
                    currentConditionNode.getAttributes().getNamedItem("date").getNodeValue());

//            if (mNeedDownloadIcons) {
//                weatherInfo.setCurrentConditionIcon(ImageUtils.getBitmapFromWeb(
//                        weatherInfo.getCurrentConditionIconURL()));
//            }

            for (int i = 0; i < FORECAST_INFO_MAX_SIZE; i++) {
                this.parseForecastInfo(weatherInfo.getForecastInfoList().get(i), doc, i);
            }

        } catch (NullPointerException e) {
            weatherInfo = null;
        }

        return weatherInfo;
    }

    private void parseForecastInfo(final WeatherInfo.ForecastInfo forecastInfo, final Document doc, final int index) {
        Node forecast1ConditionNode = doc.getElementsByTagName("yweather:forecast").item(index);
        forecastInfo.setForecastCode(Integer.parseInt(
                forecast1ConditionNode.getAttributes().getNamedItem("code").getNodeValue()
        ));
        forecastInfo.setForecastText(
                forecast1ConditionNode.getAttributes().getNamedItem("text").getNodeValue());
        forecastInfo.setForecastDate(
                forecast1ConditionNode.getAttributes().getNamedItem("date").getNodeValue());
        forecastInfo.setForecastDay(
                forecast1ConditionNode.getAttributes().getNamedItem("day").getNodeValue());
        int highC = Integer.parseInt(forecast1ConditionNode.getAttributes().getNamedItem("high").getNodeValue());
        forecastInfo.setForecastTempHigh(highC);
        int lowC = Integer.parseInt(forecast1ConditionNode.getAttributes().getNamedItem("low").getNodeValue());
        forecastInfo.setForecastTempLow(lowC);
//        if (mNeedDownloadIcons) {
//            forecastInfo.setForecastConditionIcon(
//                    ImageUtils.getBitmapFromWeb(forecastInfo.getForecastConditionIconURL()));
//        }
    }
}
