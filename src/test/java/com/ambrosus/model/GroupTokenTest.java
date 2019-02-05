package com.ambrosus.model;

import com.ambrosus.AmbrosusSDK;
import org.junit.Test;

import static org.junit.Assert.*;

public class GroupTokenTest {

    @Test
    public void createToken() {
        AmbrosusSDK sdk = new AmbrosusSDK(new AmbrosusSDK.Config("https://not-matters.com", "0xbeefc0de"));
        sdk.init();
        String token = sdk.createGroupToken(1558841873L);
        assertEquals("eyJzaWduYXR1cmUiOiIweDZiZmVmY2M0M2ExZjg3MWRkNmQyM2Y4MTFlNTMxZGU4YjBkZTVlMTIyM2U5M2I1ODgzZWY2NTdhZmJlMmYzNmQwMWJkOTdmZGI4YjZkMTUzZjU2OTY1MDcyNzNkMmYzOGVhYWY4Y2M4OWQ1MzA2MGNjOThhMTU1N2E2MjMzN2E1MWMiLCJpZERhdGEiOnsiY3JlYXRlZEJ5IjoiMHhiMTFFNkQ4OGE4MEEwOUNmMjVCRDEyZjFiOTE4QUU2OEI0MTM0RjY3IiwidmFsaWRVbnRpbCI6MTU1ODg0MTg3M319", token);
    }
}