/*
Copyright: Ambrosus Technologies GmbH
Email: tech@ambrosus.com

This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.

This Source Code Form is "Incompatible With Secondary Licenses", as defined by the Mozilla Public License, v. 2.0.
*/

package model;

import com.ambrosus.model.AmbrosusType;
import com.ambrosus.model.Asset;
import com.ambrosus.model.MetaData;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class AmbrosusTypeTests {

    private String BUNDLE_ID;
    private String CREATED_BY;
    private Long ENTITY_UPLOAD_TIMESTAMP;
    private Long TIMESTAMP;
    private Asset.Builder typeBuilder;


    @Before
    public void init() {

        BUNDLE_ID = "bundleId";
        CREATED_BY = "createdBy";
        ENTITY_UPLOAD_TIMESTAMP = 123L;
        TIMESTAMP = 1234L;

        typeBuilder = new Asset.Builder();
        typeBuilder
                .setMetaData(new MetaData(BUNDLE_ID, ENTITY_UPLOAD_TIMESTAMP))
                .setTimestamp(TIMESTAMP)
                .setCreatedBy(CREATED_BY);

    }


    @Test
    public void builderIsCorrect() {

        assertEquals(new MetaData(BUNDLE_ID, ENTITY_UPLOAD_TIMESTAMP), typeBuilder.getMetaData());
        assertEquals(CREATED_BY, typeBuilder.getCreatedBy());
        assertEquals(TIMESTAMP, typeBuilder.getTimestamp());

        AmbrosusType asset = typeBuilder.build();

        assertEquals(CREATED_BY, asset.getCreatedBy());
        assertEquals(new MetaData(BUNDLE_ID, ENTITY_UPLOAD_TIMESTAMP), asset.getMetaData());
        assertEquals(TIMESTAMP, asset.getTimestamp());

    }
}
