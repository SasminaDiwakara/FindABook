package lk.jiat.fiadabook.service;

import com.google.gson.JsonObject;
import lk.jiat.fiadabook.entity.City;
import lk.jiat.fiadabook.util.AppUtil;
import lk.jiat.fiadabook.util.HibernateUtil;
import org.hibernate.Session;

import java.util.List;

public class CityService {

    public String loadCities(){

        JsonObject responseJson = new JsonObject();

        Session session = HibernateUtil.getSessionFactory().openSession();
        List<City> cities = session.createQuery("FROM City c", City.class).getResultList();
        responseJson.add("cities",AppUtil.GSON.toJsonTree(cities));
        session.close();

        return  AppUtil.GSON.toJson(responseJson);

    }


}

