/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package model;

import com.ambrosus.model.Account;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import utils.TestUtils;

import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class AccountTests {

    private Account account;
    private Account.Builder accountBuilder;
    private Gson gson;

    private String ADDRESS;
    private String REGISTERED_BY;
    private Integer ACCESS_LEVEL;
    private Long REGISTERED_ON;


    @Before
    public void init() {

        ADDRESS = "0x9566AC7630F7075a981670709de09ff9c3032D9c";
        REGISTERED_BY = "0xb0B0aA0d17C9b2B527bBc9D69bd093ab47fEBEb0";
        REGISTERED_ON = 1536223596L;
        ACCESS_LEVEL = 1;

        accountBuilder = new Account.Builder()
                .setAddress(ADDRESS)
                .setAccessLevel(ACCESS_LEVEL)
                .addPermission(Account.Permission.CREATE_ENTITY)
                .addPermission(Account.Permission.REGISTER_ACCOUNT)
                .setRegisteredBy(REGISTERED_BY)
                .setRegisteredOn(REGISTERED_ON);

        account = accountBuilder.build();

        GsonBuilder gB = new GsonBuilder();
        gB.registerTypeAdapter(Account.class, new Account.Adapter());

        gson = gB.create();
    }


    @Test
    public void equalsIsRedefined() {

        Account.Builder accountBuilder = new Account.Builder();
        accountBuilder
                .setAccessLevel(ACCESS_LEVEL)
                .setAddress(ADDRESS)
                .setRegisteredBy(REGISTERED_BY)
                .setRegisteredOn(REGISTERED_ON)
                .addPermission(Account.Permission.CREATE_ENTITY)
                .addPermission(Account.Permission.REGISTER_ACCOUNT);

        Account acc = accountBuilder.build();


        assertEquals(account, account);
        assertEquals(account, acc);
        assertEquals(account.hashCode(), acc.hashCode());

        assertNotEquals(account, null);

        accountBuilder.setAccessLevel(2);
        assertNotEquals(account, accountBuilder.build());
    }


    @Test
    public void gettersAreCorrect() {
        assertEquals(ACCESS_LEVEL, account.getAccessLevel());
        assertEquals(ADDRESS, account.getAddress());
        assertEquals(REGISTERED_BY, account.getRegisteredBy());
        assertEquals(REGISTERED_ON, account.getRegisteredOn());
        assertEquals(Arrays.asList(Account.Permission.CREATE_ENTITY,
                Account.Permission.REGISTER_ACCOUNT), account.getPermissions());
    }


    @Test
    public void builderIsCorrect() {

        assertEquals(ADDRESS, accountBuilder.getAddress());
        assertEquals(ACCESS_LEVEL, accountBuilder.getAccessLevel());
        assertEquals(Arrays.asList(
                Account.Permission.CREATE_ENTITY,
                Account.Permission.REGISTER_ACCOUNT),
                accountBuilder.getPermissions());
        assertEquals(REGISTERED_BY, accountBuilder.getRegisteredBy());
        assertEquals(REGISTERED_ON, accountBuilder.getRegisteredOn());

        Account account = accountBuilder.build();

        assertEquals(accountBuilder.getAddress(), account.getAddress());
        assertEquals(accountBuilder.getAccessLevel(), account.getAccessLevel());
        assertEquals(accountBuilder.getPermissions(), account.getPermissions());
        assertEquals(accountBuilder.getRegisteredBy(), account.getRegisteredBy());
        assertEquals(accountBuilder.getRegisteredOn(), account.getRegisteredOn());
    }


    @Test
    public void serializationIsCorrect() {

        JsonObject jsonAccount = TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_account.json");
        String expectedString = jsonAccount.toString();
        assertEquals(expectedString, gson.toJson(account));

    }


    @Test
    public void deserializationIsCorrect() {

        JsonObject jsonAccount = TestUtils.readJson(TestUtils.PATH_PREFIX + "valid_account.json");

        Account account = gson.fromJson(jsonAccount, Account.class);

        assertEquals(ADDRESS, account.getAddress());
        assertEquals(ACCESS_LEVEL, account.getAccessLevel());
        assertEquals(Arrays.asList(Account.Permission.CREATE_ENTITY,
                Account.Permission.REGISTER_ACCOUNT), account.getPermissions());
        assertEquals(REGISTERED_BY, account.getRegisteredBy());
        assertEquals(REGISTERED_ON, account.getRegisteredOn());
    }

}
