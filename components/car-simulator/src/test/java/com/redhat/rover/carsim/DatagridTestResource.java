package com.redhat.rover.carsim;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import java.util.Collections;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.redhat.rover.carsim.clients.model.Zone;
import com.redhat.rover.carsim.clients.model.ZoneSpec;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class DatagridTestResource implements QuarkusTestResourceLifecycleManager {

	private Jsonb jsonb = JsonbBuilder.create();
	private WireMockServer wireMockServer;
	
	@Override
	public Map<String, String> start() {

		wireMockServer = new WireMockServer(options().dynamicPort());
		wireMockServer.start(); 
		wireMockServer.stubFor(get(urlMatching("/zones/.*")).willReturn(
				aResponse().withHeader("Content-Type", "application/json").withBody(jsonb.toJson(dummyZone()))));

		return Collections.singletonMap("com.redhat.rover.carsim.datagrid.url", wireMockServer.baseUrl());
	}

	private static Zone dummyZone() {
		Zone zone = new Zone();
		ZoneSpec spec = new ZoneSpec();
		spec.setName("Testzone");
		zone.setSpec(spec);
		return zone;
	}

	@Override
	public void stop() {
		if (null != wireMockServer) {
			wireMockServer.stop();
		}
	}

}
