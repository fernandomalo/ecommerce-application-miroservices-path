package com.fernando.microservices.shipping_rules_service.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.fernando.microservices.shipping_rules_service.entity.ShippingRule;
import com.fernando.microservices.shipping_rules_service.entity.ZoneType;
import com.fernando.microservices.shipping_rules_service.entity.Zones;
import com.fernando.microservices.shipping_rules_service.repositories.ShippingRuleRepository;
import com.fernando.microservices.shipping_rules_service.repositories.ZoneRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final ZoneRepository zoneRepository;
    private final ShippingRuleRepository shippingRuleRepository;

    // Store created zones for reference
    private final Map<String, Zones> zoneMap = new java.util.HashMap<>();

    @Override
    public void run(String... args) throws Exception {
        seedZones();
        seedShippingRules();
    }

    private void seedZones() {
        if (zoneRepository.count() > 0)
            return;

        // COUNTRIES
        Zones colombia = createZone("Colombia", ZoneType.COUNTRY, null);
        Zones china = createZone("China", ZoneType.COUNTRY, null);

        // COLOMBIAN DEPARTMENTS
        Zones cordoba = createZone("Cordoba", ZoneType.REGION, colombia.getId());
        Zones magdalena = createZone("Magdalena", ZoneType.REGION, colombia.getId());
        Zones antioquia = createZone("Antioquia", ZoneType.REGION, colombia.getId());
        Zones cundinamarca = createZone("Cundinamarca", ZoneType.REGION, colombia.getId());
        Zones valle = createZone("Valle del Cauca", ZoneType.REGION, colombia.getId());
        Zones atlantico = createZone("Atlantico", ZoneType.REGION, colombia.getId());
        Zones bolivar = createZone("Bolivar", ZoneType.REGION, colombia.getId());
        Zones santander = createZone("Santander", ZoneType.REGION, colombia.getId());
        Zones boyaca = createZone("Boyaca", ZoneType.REGION, colombia.getId());
        Zones narino = createZone("Nariño", ZoneType.REGION, colombia.getId());
        Zones tolima = createZone("Tolima", ZoneType.REGION, colombia.getId());
        Zones caldas = createZone("Caldas", ZoneType.REGION, colombia.getId());
        Zones risaralda = createZone("Risaralda", ZoneType.REGION, colombia.getId());
        Zones quindio = createZone("Quindio", ZoneType.REGION, colombia.getId());
        Zones norteSantander = createZone("Norte de Santander", ZoneType.REGION, colombia.getId());
        Zones huila = createZone("Huila", ZoneType.REGION, colombia.getId());
        Zones cauca = createZone("Cauca", ZoneType.REGION, colombia.getId());
        Zones cesar = createZone("Cesar", ZoneType.REGION, colombia.getId());
        Zones sucre = createZone("Sucre", ZoneType.REGION, colombia.getId());
        Zones laGuajira = createZone("La Guajira", ZoneType.REGION, colombia.getId());

        // CHINESE REGIONS
        Zones beijin = createZone("Beijin", ZoneType.REGION, china.getId());
        Zones shanghai = createZone("Shanghai", ZoneType.REGION, china.getId());
        Zones guangdong = createZone("Guangdong", ZoneType.REGION, china.getId());
        Zones shenzhen = createZone("Shenzhen", ZoneType.REGION, china.getId());

        // CITIES BY DEPARTMENT
        // Cordoba cities
        createZone("Monteria", ZoneType.CITY, cordoba.getId());
        createZone("Cerete", ZoneType.CITY, cordoba.getId());
        createZone("Lorica", ZoneType.CITY, cordoba.getId());
        createZone("Sahagun", ZoneType.CITY, cordoba.getId());

        // Magdalena cities
        createZone("Santa Marta", ZoneType.CITY, magdalena.getId());
        createZone("Cienaga", ZoneType.CITY, magdalena.getId());
        createZone("Fundacion", ZoneType.CITY, magdalena.getId());
        createZone("El Banco", ZoneType.CITY, magdalena.getId());

        // Antioquia cities
        createZone("Medellin", ZoneType.CITY, antioquia.getId());
        createZone("Bello", ZoneType.CITY, antioquia.getId());
        createZone("Itagui", ZoneType.CITY, antioquia.getId());
        createZone("Envigado", ZoneType.CITY, antioquia.getId());
        createZone("Rionegro", ZoneType.CITY, antioquia.getId());

        // Cundinamarca
        createZone("Bogota", ZoneType.CITY, cundinamarca.getId());
        createZone("Soacha", ZoneType.CITY, cundinamarca.getId());
        createZone("Zipaquirá", ZoneType.CITY, cundinamarca.getId());
        createZone("Facatativá", ZoneType.CITY, cundinamarca.getId());
        createZone("Chía", ZoneType.CITY, cundinamarca.getId());

        // Valle del Cauca
        createZone("Cali", ZoneType.CITY, valle.getId());
        createZone("Palmira", ZoneType.CITY, valle.getId());
        createZone("Buenaventura", ZoneType.CITY, valle.getId());
        createZone("Tuluá", ZoneType.CITY, valle.getId());
        createZone("Cartago", ZoneType.CITY, valle.getId());

        // Atlantico
        createZone("Barranquilla", ZoneType.CITY, atlantico.getId());
        createZone("Soledad", ZoneType.CITY, atlantico.getId());
        createZone("Malambo", ZoneType.CITY, atlantico.getId());
        createZone("Puerto Colombia", ZoneType.CITY, atlantico.getId());

        // Bolivar
        createZone("Cartagena", ZoneType.CITY, bolivar.getId());
        createZone("Magangué", ZoneType.CITY, bolivar.getId());
        createZone("Turbaco", ZoneType.CITY, bolivar.getId());
        createZone("Arjona", ZoneType.CITY, bolivar.getId());

        // Santander
        createZone("Bucaramanga", ZoneType.CITY, santander.getId());
        createZone("Floridablanca", ZoneType.CITY, santander.getId());
        createZone("Girón", ZoneType.CITY, santander.getId());
        createZone("Piedecuesta", ZoneType.CITY, santander.getId());

        // Boyaca
        createZone("Tunja", ZoneType.CITY, boyaca.getId());
        createZone("Duitama", ZoneType.CITY, boyaca.getId());
        createZone("Sogamoso", ZoneType.CITY, boyaca.getId());
        createZone("Paipa", ZoneType.CITY, boyaca.getId());

        // Nariño
        createZone("Pasto", ZoneType.CITY, narino.getId());
        createZone("Ipiales", ZoneType.CITY, narino.getId());
        createZone("Tumaco", ZoneType.CITY, narino.getId());

        // Tolima
        createZone("Ibagué", ZoneType.CITY, tolima.getId());
        createZone("Espinal", ZoneType.CITY, tolima.getId());
        createZone("Chaparral", ZoneType.CITY, tolima.getId());

        // Coffee Axis
        createZone("Manizales", ZoneType.CITY, caldas.getId());
        createZone("Pereira", ZoneType.CITY, risaralda.getId());
        createZone("Armenia", ZoneType.CITY, quindio.getId());

        // Norte de Santander
        createZone("Cúcuta", ZoneType.CITY, norteSantander.getId());
        createZone("Ocaña", ZoneType.CITY, norteSantander.getId());

        // Huila
        createZone("Neiva", ZoneType.CITY, huila.getId());
        createZone("Pitalito", ZoneType.CITY, huila.getId());

        // Cauca
        createZone("Popayán", ZoneType.CITY, cauca.getId());
        createZone("Santander de Quilichao", ZoneType.CITY, cauca.getId());

        // Cesar
        createZone("Valledupar", ZoneType.CITY, cesar.getId());
        createZone("Aguachica", ZoneType.CITY, cesar.getId());

        // Sucre
        createZone("Sincelejo", ZoneType.CITY, sucre.getId());

        // La Guajira
        createZone("Riohacha", ZoneType.CITY, laGuajira.getId());

        // CHINESE CITIES
        List<String> beijingDistricts = List.of(
                "Dongcheng", "Xicheng", "Chaoyang", "Fengtai", "Shijingshan",
                "Haidian", "Mentougou", "Fangshan", "Tongzhou", "Shunyi",
                "Changping", "Daxing", "Huairou", "Pinggu", "Miyun", "Yanqing");

        for (String district : beijingDistricts) {
            createZone(district, ZoneType.CITY, beijin.getId());
        }

        List<String> shanghaiDistricts = List.of("Pudong", "Huangpu", "Jing'an", "Xuhui", "Changning");
        for (String district : shanghaiDistricts) {
            createZone(district, ZoneType.CITY, shanghai.getId());
        }

        List<String> guangdongCities = List.of("Guangzhou", "Dongguan", "Foshan");
        for (String city : guangdongCities) {
            createZone(city, ZoneType.CITY, guangdong.getId());
        }

        List<String> shenzhenDistricts = List.of("Nanshan", "Futian", "Luohu", "Bao'an");
        for (String district : shenzhenDistricts) {
            createZone(district, ZoneType.CITY, shenzhen.getId());
        }
    }

    private Zones createZone(String name, ZoneType type, Long parentId) {
        Zones zone = new Zones();
        zone.setName(name);
        zone.setType(type);
        zone.setParentId(parentId);
        Zones saved = zoneRepository.save(zone);
        zoneMap.put(name, saved);
        return saved;
    }

    private void seedShippingRules() {
        if (shippingRuleRepository.count() > 0)
            return;

        List<ShippingRule> rules = new ArrayList<>();
        // Use a set to track unique origin-destination pairs
        Set<String> uniqueRules = new HashSet<>();

        // Define departments and their cities
        Map<String, List<String>> departmentsWithCities = new HashMap<>();

        departmentsWithCities.put("Cordoba", List.of("Monteria", "Cerete", "Lorica", "Sahagun"));
        departmentsWithCities.put("Magdalena", List.of("Santa Marta", "Cienaga", "Fundacion", "El Banco"));
        departmentsWithCities.put("Antioquia", List.of("Medellin", "Bello", "Itagui", "Envigado", "Rionegro"));
        departmentsWithCities.put("Cundinamarca", List.of("Bogota", "Soacha", "Zipaquirá", "Facatativá", "Chía"));
        departmentsWithCities.put("Valle del Cauca", List.of("Cali", "Palmira", "Buenaventura", "Tuluá", "Cartago"));
        departmentsWithCities.put("Atlantico", List.of("Barranquilla", "Soledad", "Malambo", "Puerto Colombia"));
        departmentsWithCities.put("Bolivar", List.of("Cartagena", "Magangué", "Turbaco", "Arjona"));
        departmentsWithCities.put("Santander", List.of("Bucaramanga", "Floridablanca", "Girón", "Piedecuesta"));
        departmentsWithCities.put("Boyaca", List.of("Tunja", "Duitama", "Sogamoso", "Paipa"));
        departmentsWithCities.put("Nariño", List.of("Pasto", "Ipiales", "Tumaco"));
        departmentsWithCities.put("Tolima", List.of("Ibagué", "Espinal", "Chaparral"));
        departmentsWithCities.put("Caldas", List.of("Manizales"));
        departmentsWithCities.put("Risaralda", List.of("Pereira"));
        departmentsWithCities.put("Quindio", List.of("Armenia"));
        departmentsWithCities.put("Norte de Santander", List.of("Cúcuta", "Ocaña"));
        departmentsWithCities.put("Huila", List.of("Neiva", "Pitalito"));
        departmentsWithCities.put("Cauca", List.of("Popayán", "Santander de Quilichao"));
        departmentsWithCities.put("Cesar", List.of("Valledupar", "Aguachica"));
        departmentsWithCities.put("Sucre", List.of("Sincelejo"));
        departmentsWithCities.put("La Guajira", List.of("Riohacha"));

        // Define hub cities for each department
        Map<String, String> departmentHubs = new HashMap<>();
        departmentHubs.put("Cordoba", "Monteria");
        departmentHubs.put("Magdalena", "Santa Marta");
        departmentHubs.put("Antioquia", "Medellin");
        departmentHubs.put("Cundinamarca", "Bogota");
        departmentHubs.put("Valle del Cauca", "Cali");
        departmentHubs.put("Atlantico", "Barranquilla");
        departmentHubs.put("Bolivar", "Cartagena");
        departmentHubs.put("Santander", "Bucaramanga");
        departmentHubs.put("Boyaca", "Tunja");
        departmentHubs.put("Nariño", "Pasto");
        departmentHubs.put("Tolima", "Ibagué");
        departmentHubs.put("Caldas", "Manizales");
        departmentHubs.put("Risaralda", "Pereira");
        departmentHubs.put("Quindio", "Armenia");
        departmentHubs.put("Norte de Santander", "Cúcuta");
        departmentHubs.put("Huila", "Neiva");
        departmentHubs.put("Cauca", "Popayán");
        departmentHubs.put("Cesar", "Valledupar");
        departmentHubs.put("Sucre", "Sincelejo");
        departmentHubs.put("La Guajira", "Riohacha");

        // 1. Create intra-department shipping rules (cities within same department)
        for (Map.Entry<String, List<String>> entry : departmentsWithCities.entrySet()) {
            List<String> cities = entry.getValue();

            // Self-delivery for each city
            for (String city : cities) {
                String selfKey = city + "->" + city;
                if (!uniqueRules.contains(selfKey)) {
                    rules.add(createRule(city, city, 0.0, 1));
                    uniqueRules.add(selfKey);
                }
            }

            // Rules between different cities in the same department
            for (int i = 0; i < cities.size(); i++) {
                for (int j = i + 1; j < cities.size(); j++) {
                    String city1 = cities.get(i);
                    String city2 = cities.get(j);

                    String key1 = city1 + "->" + city2;
                    String key2 = city2 + "->" + city1;

                    if (!uniqueRules.contains(key1) && !uniqueRules.contains(key2)) {
                        Double price = 8000.0;
                        Integer days = 1;

                        // Adjust based on distance
                        if (city1.equals("Bogota") || city2.equals("Bogota")) {
                            price = 10000.0;
                        } else if (city1.equals("Medellin") || city2.equals("Medellin")) {
                            price = 7000.0;
                        } else if (city1.equals("Cali") || city2.equals("Cali")) {
                            price = 7000.0;
                        } else {
                            price = 3000.0;
                        }

                        rules.add(createRule(city1, city2, price, days));
                        rules.add(createRule(city2, city1, price, days));
                        uniqueRules.add(key1);
                        uniqueRules.add(key2);
                    }
                }
            }
        }

        // 2. Create hub connections (all cities connect to their department hub)
        for (Map.Entry<String, List<String>> entry : departmentsWithCities.entrySet()) {
            String hub = departmentHubs.get(entry.getKey());
            List<String> cities = entry.getValue();

            for (String city : cities) {
                if (!city.equals(hub)) {
                    String cityToHub = city + "->" + hub;
                    String hubToCity = hub + "->" + city;

                    if (!uniqueRules.contains(cityToHub)) {
                        rules.add(createRule(city, hub, 5000.0, 1));
                        uniqueRules.add(cityToHub);
                    }

                    if (!uniqueRules.contains(hubToCity)) {
                        rules.add(createRule(hub, city, 5000.0, 1));
                        uniqueRules.add(hubToCity);
                    }
                }
            }
        }

        // 3. Major cities inter-department rules
        List<String> majorColombianCities = List.of(
                "Bogota", "Medellin", "Cali", "Barranquilla", "Cartagena",
                "Bucaramanga", "Monteria", "Santa Marta", "Pereira", "Manizales",
                "Armenia", "Cúcuta", "Ibagué", "Neiva", "Pasto",
                "Valledupar", "Sincelejo", "Riohacha", "Tunja", "Popayán");

        // Price matrix for major cities
        Map<String, Map<String, Double>> priceMatrix = new HashMap<>();

        Map<String, Double> bogotaPrices = new HashMap<>();
        bogotaPrices.put("Medellin", 4500.0);
        bogotaPrices.put("Cali", 5000.0);
        bogotaPrices.put("Barranquilla", 8000.0);
        bogotaPrices.put("Cartagena", 8500.0);
        bogotaPrices.put("Bucaramanga", 6000.0);
        bogotaPrices.put("Santa Marta", 9000.0);
        bogotaPrices.put("Pereira", 4000.0);
        bogotaPrices.put("Manizales", 3800.0);
        bogotaPrices.put("Armenia", 3900.0);
        bogotaPrices.put("Cúcuta", 6500.0);
        bogotaPrices.put("Ibagué", 3500.0);
        bogotaPrices.put("Neiva", 4000.0);
        bogotaPrices.put("Pasto", 9500.0);
        bogotaPrices.put("Monteria", 7500.0);
        priceMatrix.put("Bogota", bogotaPrices);

        Map<String, Double> medellinPrices = new HashMap<>();
        medellinPrices.put("Cali", 4800.0);
        medellinPrices.put("Barranquilla", 7000.0);
        medellinPrices.put("Cartagena", 7500.0);
        medellinPrices.put("Bucaramanga", 4500.0);
        medellinPrices.put("Santa Marta", 8000.0);
        medellinPrices.put("Pereira", 2500.0);
        medellinPrices.put("Manizales", 2200.0);
        medellinPrices.put("Armenia", 2300.0);
        medellinPrices.put("Cúcuta", 5800.0);
        medellinPrices.put("Ibagué", 2800.0);
        medellinPrices.put("Neiva", 3500.0);
        medellinPrices.put("Pasto", 8500.0);
        medellinPrices.put("Monteria", 6000.0);
        priceMatrix.put("Medellin", medellinPrices);

        Map<String, Double> caliPrices = new HashMap<>();
        caliPrices.put("Barranquilla", 8500.0);
        caliPrices.put("Cartagena", 9000.0);
        caliPrices.put("Bucaramanga", 7000.0);
        caliPrices.put("Santa Marta", 9500.0);
        caliPrices.put("Pereira", 3800.0);
        caliPrices.put("Manizales", 4000.0);
        caliPrices.put("Armenia", 3700.0);
        caliPrices.put("Cúcuta", 8200.0);
        caliPrices.put("Ibagué", 3000.0);
        caliPrices.put("Neiva", 2500.0);
        caliPrices.put("Pasto", 4500.0);
        caliPrices.put("Monteria", 7500.0);
        priceMatrix.put("Cali", caliPrices);

        // Generate rules between major Colombian cities
        for (int i = 0; i < majorColombianCities.size(); i++) {
            String origin = majorColombianCities.get(i);

            for (int j = i + 1; j < majorColombianCities.size(); j++) {
                String dest = majorColombianCities.get(j);

                String key1 = origin + "->" + dest;
                String key2 = dest + "->" + origin;

                if (!uniqueRules.contains(key1) && !uniqueRules.contains(key2)) {
                    Double price = getPriceFromMatrix(origin, dest, priceMatrix, 5000.0);
                    int days = price <= 3000 ? 2 : (price <= 6000 ? 3 : 5);

                    rules.add(createRule(origin, dest, price, days));
                    rules.add(createRule(dest, origin, price, days));
                    uniqueRules.add(key1);
                    uniqueRules.add(key2);
                }
            }
        }

        // 4. International rules
        List<String> majorChineseCities = List.of(
                "Dongcheng", "Xicheng", "Chaoyang", "Pudong", "Huangpu",
                "Guangzhou", "Nanshan", "Futian");

        for (String colombianCity : majorColombianCities) {
            for (String chineseCity : majorChineseCities) {
                String key1 = colombianCity + "->" + chineseCity;
                String key2 = chineseCity + "->" + colombianCity;

                if (!uniqueRules.contains(key1)) {
                    rules.add(createRule(colombianCity, chineseCity, 18000.0, 15));
                    uniqueRules.add(key1);
                }

                if (!uniqueRules.contains(key2)) {
                    rules.add(createRule(chineseCity, colombianCity, 18000.0, 15));
                    uniqueRules.add(key2);
                }
            }
        }

        // 5. Additional specific city pairs and cross-department connections
        // Define which major city each department connects to
        Map<String, String> departmentToMajorCity = new HashMap<>();
        departmentToMajorCity.put("Cordoba", "Monteria");
        departmentToMajorCity.put("Magdalena", "Santa Marta");
        departmentToMajorCity.put("Antioquia", "Medellin");
        departmentToMajorCity.put("Cundinamarca", "Bogota");
        departmentToMajorCity.put("Valle del Cauca", "Cali");
        departmentToMajorCity.put("Atlantico", "Barranquilla");
        departmentToMajorCity.put("Bolivar", "Cartagena");
        departmentToMajorCity.put("Santander", "Bucaramanga");
        departmentToMajorCity.put("Boyaca", "Tunja");
        departmentToMajorCity.put("Nariño", "Pasto");
        departmentToMajorCity.put("Tolima", "Ibagué");
        departmentToMajorCity.put("Caldas", "Manizales");
        departmentToMajorCity.put("Risaralda", "Pereira");
        departmentToMajorCity.put("Quindio", "Armenia");
        departmentToMajorCity.put("Norte de Santander", "Cúcuta");
        departmentToMajorCity.put("Huila", "Neiva");
        departmentToMajorCity.put("Cauca", "Popayán");
        departmentToMajorCity.put("Cesar", "Valledupar");
        departmentToMajorCity.put("Sucre", "Sincelejo");
        departmentToMajorCity.put("La Guajira", "Riohacha");

        // Connect all secondary cities to major cities from other departments
        Map<String, List<String>> secondaryCitiesByDepartment = new HashMap<>();
        secondaryCitiesByDepartment.put("Cordoba", List.of("Cerete", "Lorica", "Sahagun"));
        secondaryCitiesByDepartment.put("Magdalena", List.of("Cienaga", "Fundacion", "El Banco"));
        secondaryCitiesByDepartment.put("Antioquia", List.of("Bello", "Itagui", "Envigado", "Rionegro"));
        secondaryCitiesByDepartment.put("Cundinamarca", List.of("Soacha", "Zipaquirá", "Facatativá", "Chía"));
        secondaryCitiesByDepartment.put("Valle del Cauca", List.of("Palmira", "Buenaventura", "Tuluá", "Cartago"));
        secondaryCitiesByDepartment.put("Atlantico", List.of("Soledad", "Malambo", "Puerto Colombia"));
        secondaryCitiesByDepartment.put("Bolivar", List.of("Magangué", "Turbaco", "Arjona"));
        secondaryCitiesByDepartment.put("Santander", List.of("Floridablanca", "Girón", "Piedecuesta"));
        secondaryCitiesByDepartment.put("Boyaca", List.of("Duitama", "Sogamoso", "Paipa"));
        secondaryCitiesByDepartment.put("Nariño", List.of("Ipiales", "Tumaco"));
        secondaryCitiesByDepartment.put("Tolima", List.of("Espinal", "Chaparral"));
        secondaryCitiesByDepartment.put("Norte de Santander", List.of("Ocaña"));
        secondaryCitiesByDepartment.put("Huila", List.of("Pitalito"));
        secondaryCitiesByDepartment.put("Cauca", List.of("Santander de Quilichao"));
        secondaryCitiesByDepartment.put("Cesar", List.of("Aguachica"));

        // 5.1 Connect secondary cities to major cities from other departments
        for (Map.Entry<String, List<String>> entry : secondaryCitiesByDepartment.entrySet()) {
            String department = entry.getKey();
            List<String> secondaryCities = entry.getValue();
            String departmentMajorCity = departmentToMajorCity.get(department);
            
            for (String secondaryCity : secondaryCities) {
                for (String majorCity : majorColombianCities) {
                    if (!majorCity.equals(departmentMajorCity)) {
                        Double price = calculateCrossDepartmentPrice(secondaryCity, majorCity, departmentMajorCity);
                        Integer days = calculateDeliveryDays(price);
                        addSpecificRule(rules, uniqueRules, secondaryCity, majorCity, price, days);
                    }
                }
            }
        }

        // 5.2 Connect secondary cities to secondary cities from other departments
        addSpecificRule(rules, uniqueRules, "Cerete", "Sincelejo", 7000.0, 2);
        addSpecificRule(rules, uniqueRules, "Lorica", "Cartagena", 8000.0, 2);
        addSpecificRule(rules, uniqueRules, "Sahagun", "Monteria", 5000.0, 1);
        addSpecificRule(rules, uniqueRules, "Cienaga", "Santa Marta", 5000.0, 1);
        addSpecificRule(rules, uniqueRules, "Fundacion", "Valledupar", 8000.0, 2);
        addSpecificRule(rules, uniqueRules, "El Banco", "Barranquilla", 9000.0, 2);
        addSpecificRule(rules, uniqueRules, "Bello", "Pereira", 12000.0, 2);
        addSpecificRule(rules, uniqueRules, "Itagui", "Manizales", 10000.0, 2);
        addSpecificRule(rules, uniqueRules, "Soacha", "Neiva", 15000.0, 3);
        addSpecificRule(rules, uniqueRules, "Zipaquirá", "Tunja", 12000.0, 2);
        addSpecificRule(rules, uniqueRules, "Facatativá", "Ibagué", 14000.0, 3);
        addSpecificRule(rules, uniqueRules, "Palmira", "Popayán", 10000.0, 2);
        addSpecificRule(rules, uniqueRules, "Buenaventura", "Cali", 15000.0, 3);
        addSpecificRule(rules, uniqueRules, "Tuluá", "Pereira", 8000.0, 2);
        addSpecificRule(rules, uniqueRules, "Cartago", "Manizales", 9000.0, 2);
        addSpecificRule(rules, uniqueRules, "Soledad", "Barranquilla", 3000.0, 1);
        addSpecificRule(rules, uniqueRules, "Puerto Colombia", "Cartagena", 10000.0, 2);
        addSpecificRule(rules, uniqueRules, "Magangué", "Sincelejo", 8000.0, 2);
        addSpecificRule(rules, uniqueRules, "Magangué", "Monteria", 10000.0, 2);
        addSpecificRule(rules, uniqueRules, "Floridablanca", "Cúcuta", 12000.0, 2);
        addSpecificRule(rules, uniqueRules, "Duitama", "Bucaramanga", 18000.0, 3);
        addSpecificRule(rules, uniqueRules, "Ipiales", "Popayán", 15000.0, 3);
        addSpecificRule(rules, uniqueRules, "Tumaco", "Pasto", 18000.0, 4);
        addSpecificRule(rules, uniqueRules, "Espinal", "Neiva", 10000.0, 2);
        addSpecificRule(rules, uniqueRules, "Ocaña", "Bucaramanga", 15000.0, 3);
        addSpecificRule(rules, uniqueRules, "Pitalito", "Popayán", 12000.0, 3);
        addSpecificRule(rules, uniqueRules, "Santander de Quilichao", "Cali", 8000.0, 2);
        addSpecificRule(rules, uniqueRules, "Aguachica", "Bucaramanga", 12000.0, 3);

        // 5.3 Connect major cities to all secondary cities in other departments
        for (String majorCity : majorColombianCities) {
            for (Map.Entry<String, List<String>> entry : secondaryCitiesByDepartment.entrySet()) {
                String department = entry.getKey();
                String departmentMajorCity = departmentToMajorCity.get(department);
                List<String> secondaryCities = entry.getValue();
                
                for (String secondaryCity : secondaryCities) {
                    if (!majorCity.equals(departmentMajorCity)) {
                        Double price = calculateMajorToSecondaryPrice(majorCity, secondaryCity, departmentMajorCity);
                        Integer days = calculateDeliveryDays(price);
                        addSpecificRule(rules, uniqueRules, majorCity, secondaryCity, price, days);
                    }
                }
            }
        }

        // 5.4 Specific rules between department cities
        addSpecificRule(rules, uniqueRules, "Manizales", "Pereira", 20000.0, 2);
        addSpecificRule(rules, uniqueRules, "Manizales", "Armenia", 22000.0, 2);
        addSpecificRule(rules, uniqueRules, "Pereira", "Armenia", 18000.0, 2);
        addSpecificRule(rules, uniqueRules, "Manizales", "Medellin", 35000.0, 3);
        addSpecificRule(rules, uniqueRules, "Barranquilla", "Cartagena", 25000.0, 2);
        addSpecificRule(rules, uniqueRules, "Barranquilla", "Santa Marta", 28000.0, 3);
        addSpecificRule(rules, uniqueRules, "Cartagena", "Santa Marta", 30000.0, 3);
        addSpecificRule(rules, uniqueRules, "Sincelejo", "Monteria", 20000.0, 2);
        addSpecificRule(rules, uniqueRules, "Sincelejo", "Cartagena", 25000.0, 2);
        addSpecificRule(rules, uniqueRules, "Riohacha", "Santa Marta", 35000.0, 4);
        addSpecificRule(rules, uniqueRules, "Valledupar", "Santa Marta", 30000.0, 3);
        addSpecificRule(rules, uniqueRules, "Valledupar", "Barranquilla", 32000.0, 3);
        addSpecificRule(rules, uniqueRules, "Bogota", "Tunja", 25000.0, 2);
        addSpecificRule(rules, uniqueRules, "Bogota", "Ibagué", 30000.0, 3);
        addSpecificRule(rules, uniqueRules, "Bogota", "Neiva", 35000.0, 3);
        addSpecificRule(rules, uniqueRules, "Bogota", "Bucaramanga", 40000.0, 4);
        addSpecificRule(rules, uniqueRules, "Cali", "Popayán", 25000.0, 2);
        addSpecificRule(rules, uniqueRules, "Cali", "Pasto", 40000.0, 4);
        addSpecificRule(rules, uniqueRules, "Popayán", "Pasto", 30000.0, 3);
        addSpecificRule(rules, uniqueRules, "Buenaventura", "Cali", 20000.0, 2);
        addSpecificRule(rules, uniqueRules, "Cúcuta", "Bucaramanga", 45000.0, 4);
        addSpecificRule(rules, uniqueRules, "Cúcuta", "Ocaña", 25000.0, 2);
        addSpecificRule(rules, uniqueRules, "Ipiales", "Pasto", 20000.0, 2);
        addSpecificRule(rules, uniqueRules, "Riohacha", "Valledupar", 28000.0, 3);
        addSpecificRule(rules, uniqueRules, "Medellin", "Pereira", 35000.0, 3);
        addSpecificRule(rules, uniqueRules, "Medellin", "Manizales", 32000.0, 3);
        addSpecificRule(rules, uniqueRules, "Medellin", "Bucaramanga", 40000.0, 4);
        addSpecificRule(rules, uniqueRules, "Cali", "Neiva", 35000.0, 3);

        shippingRuleRepository.saveAll(rules);
        
        System.out.println("Total unique shipping rules created: " + rules.size());
    }

    // Helper method to add specific rules with duplicate checking
    private void addSpecificRule(List<ShippingRule> rules, Set<String> uniqueRules,
            String city1, String city2, Double price, Integer days) {
        String key1 = city1 + "->" + city2;
        String key2 = city2 + "->" + city1;

        if (!uniqueRules.contains(key1)) {
            rules.add(createRule(city1, city2, price, days));
            uniqueRules.add(key1);
        }

        if (!uniqueRules.contains(key2)) {
            rules.add(createRule(city2, city1, price, days));
            uniqueRules.add(key2);
        }
    }

    private Double getPriceFromMatrix(String origin, String dest, Map<String, Map<String, Double>> matrix,
            Double defaultPrice) {
        if (matrix.containsKey(origin) && matrix.get(origin).containsKey(dest)) {
            return matrix.get(origin).get(dest);
        }
        if (matrix.containsKey(dest) && matrix.get(dest).containsKey(origin)) {
            return matrix.get(dest).get(origin);
        }
        return defaultPrice;
    }

    private ShippingRule createRule(String origin, String destination, Double price, Integer days) {
        ShippingRule rule = new ShippingRule();
        rule.setOriginZone(origin);
        rule.setDestinationZone(destination);
        rule.setPrice(price);
        rule.setDurationTime(days);
        return rule;
    }

    private Double calculateCrossDepartmentPrice(String secondaryCity, String majorCity, String departmentMajorCity) {
        if (majorCity.equals("Bogota")) {
            if (departmentMajorCity.equals("Medellin")) return 25000.0;
            if (departmentMajorCity.equals("Cali")) return 30000.0;
            if (departmentMajorCity.equals("Barranquilla")) return 35000.0;
            if (departmentMajorCity.equals("Cartagena")) return 38000.0;
            if (departmentMajorCity.equals("Santa Marta")) return 42000.0;
            if (departmentMajorCity.equals("Monteria")) return 38000.0;
            if (departmentMajorCity.equals("Bucaramanga")) return 20000.0;
            if (departmentMajorCity.equals("Pereira")) return 18000.0;
            if (departmentMajorCity.equals("Manizales")) return 17000.0;
            return 25000.0;
        } else if (majorCity.equals("Medellin")) {
            if (departmentMajorCity.equals("Bogota")) return 22000.0;
            if (departmentMajorCity.equals("Cali")) return 20000.0;
            if (departmentMajorCity.equals("Barranquilla")) return 28000.0;
            if (departmentMajorCity.equals("Cartagena")) return 30000.0;
            if (departmentMajorCity.equals("Santa Marta")) return 32000.0;
            if (departmentMajorCity.equals("Pereira")) return 10000.0;
            if (departmentMajorCity.equals("Manizales")) return 8000.0;
            return 20000.0;
        } else if (majorCity.equals("Cali")) {
            if (departmentMajorCity.equals("Bogota")) return 25000.0;
            if (departmentMajorCity.equals("Medellin")) return 20000.0;
            if (departmentMajorCity.equals("Popayán")) return 10000.0;
            if (departmentMajorCity.equals("Pasto")) return 18000.0;
            return 22000.0;
        }
        return 15000.0;
    }

    private Double calculateMajorToSecondaryPrice(String majorCity, String secondaryCity, String departmentMajorCity) {
        return calculateCrossDepartmentPrice(secondaryCity, majorCity, departmentMajorCity);
    }

    private Integer calculateDeliveryDays(Double price) {
        if (price <= 15000) return 2;
        if (price <= 25000) return 3;
        if (price <= 40000) return 4;
        return 5;
    }
}