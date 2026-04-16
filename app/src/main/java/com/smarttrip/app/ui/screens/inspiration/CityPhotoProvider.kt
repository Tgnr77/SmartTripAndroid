package com.smarttrip.app.ui.screens.inspiration

/**
 * Fournit des URLs de photos de ville fiables par code IATA.
 * Sources : Unsplash CDN (photos curatées) + picsum.photos (fallback stable par seed).
 */
object CityPhotoProvider {

    private const val Q = "?w=600&h=220&fit=crop&auto=format&q=80"
    private fun u(id: String) = "https://images.unsplash.com/photo-$id$Q"

    private val PHOTOS = mapOf(
        // ── Europe ────────────────────────────────────────────────────────
        "CDG" to u("1502602898657-3e91760cbb34"),  // Paris – Tour Eiffel
        "ORY" to u("1502602898657-3e91760cbb34"),
        "LHR" to u("1513635269975-59663e0ac1ad"),  // London – Big Ben
        "LGW" to u("1513635269975-59663e0ac1ad"),
        "STN" to u("1513635269975-59663e0ac1ad"),
        "MAD" to u("1539037116277-4db20889f2d4"),  // Madrid – Gran Vía
        "BCN" to u("1583422409516-2895a77efded"),  // Barcelona – Sagrada Família
        "FCO" to u("1552832230-c0197dd311b5"),     // Rome – Colisée
        "CIA" to u("1552832230-c0197dd311b5"),
        "MXP" to u("1555996060-1a9b39ee9d1c"),     // Milan – Duomo
        "LIN" to u("1555996060-1a9b39ee9d1c"),
        "AMS" to u("1534351590666-13e3e96b5017"),  // Amsterdam – Canaux
        "ATH" to u("1555993539-1732b0258235"),     // Athènes – Acropole
        "LIS" to u("1555881400-74d7acaacd8b"),     // Lisbonne – Tramway
        "OPO" to u("1566378246424-3d8e1ab37e6d"),  // Porto – Ribeira
        "VIE" to u("1516550135131-a0e603a7a95e"),  // Vienne – Schönbrunn
        "PRG" to u("1541849487-a1c2a5b6a9e4"),     // Prague – Pont Charles
        "BUD" to u("1549982832-d1dbd2b2c8d6"),     // Budapest – Parlement
        "WAW" to u("1607427293702-036933bbf746"),  // Varsovie
        "ARN" to u("1508189860359-777d945909ef"),  // Stockholm
        "OSL" to u("1513519245088-0e12902e5a38"),  // Oslo
        "CPH" to u("1513622470522-26c3c8a854bc"),  // Copenhague – Nyhavn
        "HEL" to u("1559181567-c3190bfbd7d5"),     // Helsinki
        "DUB" to u("1549918864-48ac978761a4"),     // Dublin
        "BRU" to u("1559113513-d5f2c0f8df89"),     // Bruxelles – Grand-Place
        "ZRH" to u("1515488764276-beab7607c1e6"),  // Zurich
        "GVA" to u("1527095727030-87c03f7bbb59"),  // Genève – Lac
        "MUC" to u("1577996655335-f5e7b9c7f62f"),  // Munich – Marienplatz
        "FRA" to u("1467269204594-9661b134dd2b"),  // Francfort – Skyline
        "IST" to u("1524231757912-21f4fe3a7200"),  // Istanbul – Mosquée Bleue
        "SAW" to u("1524231757912-21f4fe3a7200"),
        "NCE" to u("1491557345352-5929e343eb89"),  // Nice – Promenade
        "LYS" to u("1524519683488-d99dc65c1e07"),  // Lyon – Vieux-Lyon
        "MRS" to u("1596394516093-501ba68a0ba6"),  // Marseille – Vieux-Port
        "BOD" to u("1574170609419-d7e22abb97fd"),  // Bordeaux – Place de la Bourse
        "TLS" to u("1589568149697-9f67e745e076"),  // Toulouse
        "EDI" to u("1559477177-868e4f80da89"),     // Édimbourg – Château
        "MAN" to u("1523755231516-e43fd2e8dca5"),  // Manchester
        "VCE" to u("1523906834658-6e24ef2386f9"),  // Venise – Grand Canal
        "FLR" to u("1543429776-2782fc8e3a40"),     // Florence – Duomo
        "NAP" to u("1534308983496-4fabb1a015ee"),  // Naples
        "ZAG" to u("1555990538-bda764d87d2b"),     // Zagreb
        "KEF" to u("1474690870753-1b92efa1f2d8"),  // Reykjavik – Aurores boréales
        // ── Moyen-Orient & Afrique ────────────────────────────────────────
        "DXB" to u("1512453979798-5ea266f8880c"),  // Dubaï – Burj Khalifa
        "AUH" to u("1605640840605-14ac1855827b"),  // Abu Dhabi – Mosquée Sheikh Zayed
        "DOH" to u("1611273426858-450d8e3c9fce"),  // Doha – Skyline
        "TLV" to u("1544967082-d9d25d867d66"),     // Tel Aviv – Plage
        "CAI" to u("1548786811-dd6e453ccca7"),     // Le Caire – Pyramides
        "CPT" to u("1580060839134-75a5edca2e99"),  // Le Cap – Table Mountain
        "NBO" to u("1611348586804-61bf6c080437"),  // Nairobi
        "JNB" to u("1577048982768-5cb3e7ddfa23"),  // Johannesburg
        "LOS" to u("1556075798-4825f101d819"),     // Lagos
        "ADD" to u("1602525661-0f0b2e62c6b3"),     // Addis-Abeba
        "CMN" to u("1539020140153-e479b8c22e70"),  // Casablanca
        "RAK" to u("1577147443647-81856d5151af"),  // Marrakech – Djemaa el-Fna
        "TUN" to u("1568849676085-51415703900f"),  // Tunis
        // ── Asie ──────────────────────────────────────────────────────────
        "NRT" to u("1540959733332-eab4deabeeaf"),  // Tokyo – Shibuya
        "HND" to u("1540959733332-eab4deabeeaf"),
        "KIX" to u("1493976040374-85c8e12f0c0e"),  // Osaka – Dotonbori
        "PEK" to u("1508804185872-d7badad00f7d"),  // Pékin – Grande Muraille
        "PKX" to u("1508804185872-d7badad00f7d"),
        "PVG" to u("1538428494232-9c0d8a3ab403"),  // Shanghai – Pudong
        "SHA" to u("1538428494232-9c0d8a3ab403"),
        "HKG" to u("1536599018102-9f803c140fc1"),  // Hong Kong – Victoria Harbour
        "ICN" to u("1517154421773-0529f29ea451"),  // Séoul – Gyeongbokgung
        "BKK" to u("1508009603885-50cf7c579365"),  // Bangkok – Temple
        "HKT" to u("1589394815804-964ed0be2eb5"),  // Phuket – Plage
        "SIN" to u("1525625293386-3f8f99389edd"),  // Singapour – Gardens by the Bay
        "KUL" to u("1596422846543-75c6fc197f07"),  // Kuala Lumpur – Petronas
        "CGK" to u("1555400038-63f5ba517a47"),     // Jakarta
        "DPS" to u("1537996194471-e657df975ab4"),  // Bali – Rizières
        "HAN" to u("1509030450996-dd1a26dda07a"),  // Hanoï – Lac Hoan Kiem
        "DEL" to u("1587474260584-136574528ed5"),  // Delhi – India Gate
        "BOM" to u("1595658658481-d53d3f999875"),  // Mumbai – Gateway of India
        "MAA" to u("1604497181015-76590d828b05"),  // Chennai
        "MNL" to u("1570159512614-7a2d63d3ce84"),  // Manille
        "RGN" to u("1528181304800-259b08848526"),  // Yangon – Shwedagon
        // ── Amériques ─────────────────────────────────────────────────────
        "JFK" to u("1485871981521-5b1fd3805eee"),  // New York – Skyline
        "EWR" to u("1485871981521-5b1fd3805eee"),
        "LGA" to u("1485871981521-5b1fd3805eee"),
        "LAX" to u("1534430480872-3498386e7856"),  // Los Angeles
        "MIA" to u("1533106497176-45ae19e68ba2"),  // Miami – South Beach
        "FLL" to u("1533106497176-45ae19e68ba2"),
        "SFO" to u("1501594907352-04cda38ebc29"),  // San Francisco – Golden Gate
        "ORD" to u("1477959858617-67f85cf4f1df"),  // Chicago
        "MDW" to u("1477959858617-67f85cf4f1df"),
        "LAS" to u("1581351721010-8cf859cb14a4"),  // Las Vegas – Strip
        "YYZ" to u("1517090504586-fde19ea6066f"),  // Toronto – CN Tower
        "YUL" to u("1519178614-68673b201f36"),     // Montréal
        "YVR" to u("1559494007-d38218ee2a5c"),     // Vancouver
        "MEX" to u("1518638150340-f706e86654de"),  // Mexico City
        "CUN" to u("1552074284-5e88ef1aef18"),     // Cancún – Plage
        "GIG" to u("1483729558449-99ef09a8c325"),  // Rio – Corcovado
        "GRU" to u("1578002171197-b7f23d568b4e"),  // São Paulo – Skyline
        "CGH" to u("1578002171197-b7f23d568b4e"),
        "EZE" to u("1589909202802-8f4aadce1849"),  // Buenos Aires
        "AEP" to u("1589909202802-8f4aadce1849"),
        "LIM" to u("1526392060635-9d6019884377"),  // Lima
        "BOG" to u("1588872657578-7efd1f1555ed"),  // Bogotá
        "SCL" to u("1583416750470-965b2707b355"),  // Santiago
        // ── Océanie ───────────────────────────────────────────────────────
        "SYD" to u("1506973035872-a4ec16b8e8d9"),  // Sydney – Opéra
        "MEL" to u("1545044846-351ba102b6d5"),     // Melbourne
        "BNE" to u("1524293581917-878a6d017c71"),  // Brisbane
        "AKL" to u("1507699622108-4be3abd695ad"),  // Auckland
        "WLG" to u("1507699622108-4be3abd695ad"),
    )

    /**
     * Retourne une URL de photo pour le code IATA donné.
     * Si inconnu, retourne une photo picsum stable basée sur le nom de la ville.
     */
    fun getPhotoUrl(iataCode: String, cityName: String): String =
        PHOTOS[iataCode]
            ?: "https://picsum.photos/seed/${cityName.lowercase().replace(" ", "-")}/600/220"
}
