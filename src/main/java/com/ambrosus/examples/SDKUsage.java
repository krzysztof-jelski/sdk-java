package com.ambrosus.examples;

import com.ambrosus.AmbrosusSDK;
import com.ambrosus.commons.Location;
import com.ambrosus.commons.Transport;
import com.ambrosus.model.Asset;
import com.ambrosus.model.Event;
import com.ambrosus.network.EventQueryResponse;
import com.ambrosus.utils.BiConsumer;
import retrofit2.Call;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class SDKUsage {


    public static <T> BiConsumer<Call<T>, Throwable> emptyOp(CountDownLatch latch) {
        return (c, t) -> {
            latch.countDown();
        };
    }


    public static void main(String[] args) {


        String privateKey = null;

        try (BufferedReader keyReader = new BufferedReader(new FileReader("private_key.keystore"));
             BufferedReader tokenReader = new BufferedReader(new FileReader("token.keystore"))) {
            privateKey = keyReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        AmbrosusSDK.Config config = new AmbrosusSDK.Config(
                "https://gateway-test.ambrosus.com",
                privateKey
        );

        AmbrosusSDK sdk = new AmbrosusSDK(config);
        sdk.init();

        // Synchronization mechanism so the program doesn't exit before the web request ends
        // You probably don't need this in your program
        CountDownLatch latch = new CountDownLatch(4);

        createAssetExample(sdk, latch);
        getAssetWithEventsExample(sdk, latch);
        createEventExample(sdk, latch);
        findAssetByIdentifier(sdk, latch);

        // Other functionalities
        featuresDemo(sdk);

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }


    private static void createAssetExample(AmbrosusSDK sdk, CountDownLatch latch) {

        Asset.Builder ab = new Asset.Builder();

        sdk.createAsset(ab, response -> {

            response.ifBodyPresent(asset -> {
                System.out.println(String.format("Created asset with ID: %s", asset.getAssetId()));
            });

            latch.countDown();
        }, emptyOp(latch));

    }


    private static void getAssetWithEventsExample(AmbrosusSDK sdk, CountDownLatch latch) {

        String assetId = "0x8b0e980ff6f79cde97ae9ed268e5a41e0dfba37adf16ae340cd76a669a623e43";

        sdk.getAssetWithEvents(assetId,
                asset -> {
                    for (Event e : asset.getEventsList())
                        System.out.println(String.format("Got event %s for asset %s", e.getEventId(), assetId));

                    Set<Event> events = asset.eventsContainingType(Location.class);
                    System.out.println(String.format("This asset has %d location events.", events.size()));
                    latch.countDown();
                },
                (call, throwable) -> {
                    throwable.printStackTrace();
                    latch.countDown();
                });
    }


    private static void createEventExample(AmbrosusSDK sdk, CountDownLatch latch) {


        Event.Builder eventBuilder = new Event.Builder();

        eventBuilder
                .setAssetId("0xd7fecb935690d572056a118d56292f954ce256cf5ee54258a227c79edb702a92")
                .addEventData(new Transport("Product crossed border", "transit", "truck"))
                .addEventData(new Location(1.2, 3.4, "Event name", "City", "Country"));

        sdk.createEvent(eventBuilder, responseWrapper -> {
            if (responseWrapper.hasBody()) {
                System.out.println(String.format("Created event with ID: %s", responseWrapper.body().getEventId()));
            }

            latch.countDown();
        }, emptyOp(latch));
    }


    private static void findAssetByIdentifier(AmbrosusSDK sdk, CountDownLatch latch) {

        Map<String, String> params = new HashMap<>();
        params.put("identifier[RFID]", "E2000016000302090940BA96");

        sdk.findAssets(params, qResponse -> {
            qResponse.ifBodyPresent(q -> {
                List<Asset> results = q.getResults();
                for (Asset a : results)
                    System.out.println(String.format("Found corresponding asset: %s", a.getAssetId()));
            });

            latch.countDown();
        }, emptyOp(latch));
    }


    private static void featuresDemo(AmbrosusSDK sdk) {

        // A query response is a list of objects returned by the server
        EventQueryResponse eventQueryResponse = sdk.fromJson("src/test/resources/complete_events_for_asset.json",
                EventQueryResponse.class);

        // The results can be retrieved
        List<Event> results = eventQueryResponse.getResults();

        // We create an asset and attach the events
        Asset.Builder assetBuilder = new Asset.Builder();
        assetBuilder
                .addAllEvents(results);

        // When an Asset instance is built, the event are indexed for easy access
        Asset asset = assetBuilder.build();

        // Events can be retrieved based on the type of data they contain
        Set<Event> events = asset.eventsContainingType(Location.class);

        // Alternatively, we can directly retrieve the data sections of interest
        List<Location> locations = asset.sectionsOfType(Location.class);

        // Events are sorted by timestamp, from the most recent to least recent
        Location recentLoc = asset.firstOf(Location.class);
        Location oldestLoc = asset.lastOf(Location.class);

        // Event data sections are linked to their parent event
        Event parentEvent = recentLoc.getParentEvent();

        //
        // Note that if we give the asset builder as-is to the sdk for creating a new asset, the associated events
        // will be ignored. This is because an asset is almost exclusively made of metadata. Instead, we should first
        // create an asset and then for each event, create an event.
        //
        sdk.createAsset(assetBuilder, responseWrapper -> {

            for (Event e : assetBuilder.getEventsList()) {
                // Note that these calls will fail since the asset id in the events of the json do not match the
                // asset id we just created.
                sdk.createEvent(Event.Builder.fromExistingEvent(e), innerResponseWrapper -> {

                }, (c, t) -> {
                });
            }


        }, (c, t) -> {
        });

    }
}
