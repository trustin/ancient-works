/*
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
/*
 * @(#) $Id: WalQueueTest.java 66 2005-01-03 09:02:37Z trustin $
 */
package net.gleamynode.oil.impl.wal;

import java.io.File;

import java.util.Properties;

import junit.framework.Assert;
import junit.framework.TestCase;

import net.gleamynode.oil.Queue;
import net.gleamynode.oil.QueueIterator;
import net.gleamynode.oil.QueueReference;


/**
 * {@link TestCase} for {@link Queue} implementation in
 * <code>net.gleamynode.oil.impl.wal</code> package.
 *
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 66 $, $Date: 2005-01-03 18:02:37 +0900 (ì›”, 03  1ì›” 2005) $
 */
public class WalQueueTest extends TestCase {
    private static final int ITEMS_PER_EXTENT = 16;
    private static final int ITEM_COUNT =
        (ITEMS_PER_EXTENT * 100) + ((ITEMS_PER_EXTENT / 3) & 0xFFFFFFFE);
    private static final String DB_FILE = "test.db";
    private static final String DB_CAT_FILE = DB_FILE + ".cat";
    private static final String QUEUE_NAME = "test";
    private static final Properties PROPERTIES = new Properties();

    static {
        PROPERTIES.setProperty("logStore.file", DB_FILE);
        PROPERTIES.setProperty("maxItemsPerExtent",
                               String.valueOf(ITEMS_PER_EXTENT));
    }

    private WalDatabase db;
    private Queue queue;
    private QueueReference[] references;
    private long startTime;

    /**
     * ÁÖ¾îÁø Ç×¸ñ ¼ö¸¸Å­ÀÇ ±Ô¸ð·Î Å×½ºÆ®ÇÏ´Â Å×½ºÆ®ÄÉÀÌ½º¸¦ »ý¼ºÇÑ´Ù.
     */
    public WalQueueTest() {
    }

    public final void setUp() throws Exception {
        new File(DB_FILE).delete();
        new File(DB_CAT_FILE).delete();

        db = new WalDatabase();
        db.setProperties(PROPERTIES);
        db.open();
        db.defragment();
        queue = db.getQueue(QUEUE_NAME);

        startTime = System.currentTimeMillis();
        references = new QueueReferenceImpl[ITEM_COUNT];

        for (int i = 0; i < ITEM_COUNT; i++) {
            String iStr = String.valueOf(i); // make items have variable length
            references[i] = queue.push(iStr);
        }

        System.out.print((System.currentTimeMillis() - startTime) / 1000.0);
        System.out.print(" / ");
        startTime = System.currentTimeMillis();
    }

    public final void tearDown() throws Exception {
        System.out.println((System.currentTimeMillis() - startTime) / 1000.0);
        references = null;

        int oldSize = queue.size();
        db.close();

        db.open(ProgressMonitorImpl.INSTANCE);
        queue = db.getQueue(QUEUE_NAME);
        Assert.assertEquals(oldSize, queue.size());
        db.defragment(ProgressMonitorImpl.INSTANCE);
        db.close();
        new File(DB_FILE).delete();
        new File(DB_CAT_FILE).delete();
    }

    public void testSimpleIteration() throws Exception {
        QueueIterator it = queue.iterator();
        int i = 0;

        while (it.next()) {
            String v = (String) it.getValue();
            Assert.assertEquals(String.valueOf(i++), v);
        }

        Assert.assertEquals(ITEM_COUNT, i);
    }

    public void testSimplePopIteration() throws Exception {
        QueueIterator it = queue.iterator();
        int i = 0;

        while (it.next()) {
            String v = (String) it.getValue();
            Assert.assertEquals(String.valueOf(i), it.remove());
            Assert.assertEquals(String.valueOf(i), v);
            i++;
        }

        Assert.assertEquals(ITEM_COUNT, i);
        Assert.assertEquals(0, queue.size());
    }

    public void testSimpleRemoveIteration() throws Exception {
        QueueIterator it = queue.iterator();
        int i = 0;

        while (it.next()) {
            String v = (String) it.getValue();
            Assert.assertEquals(String.valueOf(i), v);
            Assert.assertEquals(String.valueOf(i), it.remove());
            i++;
        }

        Assert.assertEquals(ITEM_COUNT, i);
        Assert.assertEquals(0, queue.size());
        Assert.assertTrue(queue.isEmpty());
    }

    /**
     * ÀÓÀÇ À§Ä¡¿¡ ´ëÇÑ »èÁ¦ ({@link Queue#remove(QueueReference)})¸¦Å×½ºÆ®ÇÑ´Ù.
     * <p/>
     * <ul> <li>ÀÓÀÇ À§Ä¡¿¡ ´ëÇÑ »èÁ¦°¡ ¼º°øÇßÀ» ¶§ <code>true</code> ¸¦ ¸®ÅÏÇÏ´Â°¡?</li> <li>¼øÂ÷ÀûÀ¸·Î Ç×¸ñÀ»
     * pop ÇÒ ¶§ »èÁ¦Çß´ø Ç×¸ñÀº popµÇÁö ¾Ê´Â°¡?</li> </ul>
     */
    public void testRandomRemove() throws Exception {
        for (int i = 0; i < ITEM_COUNT; i += 2) {
            Assert.assertEquals(String.valueOf(i), queue.remove(references[i]));
        }

        QueueIterator it = queue.iterator();
        int i = 1;

        while (it.next()) {
            Assert.assertEquals(it.getValue(), String.valueOf(i));
            i += 2;
        }

        Assert.assertEquals(ITEM_COUNT, i - 1);
        Assert.assertEquals(ITEM_COUNT / 2, queue.size());
    }

    /**
     * {@link Queue#exists(QueueReference)})¸¦ Å×½ºÆ®ÇÑ´Ù.
     * <p/>
     * ÀÓÀÇ À§Ä¡ÀÇ Ç×¸ñÀ» »èÁ¦ÇÑ µÚ, »èÁ¦ÇÑ Ç×¸ñ¿¡ ´ëÇØ¼­´Â <code>false</code>¸¦, »èÁ¦ÇÏÁö ¾ÊÀº Ç×¸ñ¿¡ ´ëÇØ¼­´Â
     * <code>true</code> ¸¦ ¸®ÅÏÇÏ´ÂÁö È®ÀÎÇÑ´Ù.
     */
    public void testExists() throws Exception {
        for (int i = 0; i < ITEM_COUNT; i += 2) {
            Assert.assertEquals(String.valueOf(i), queue.remove(references[i]));
        }

        for (int i = 0; i < ITEM_COUNT; i++) {
            if ((i % 2) == 0) {
                Assert.assertFalse(queue.exists(references[i]));
            } else {
                Assert.assertTrue(queue.exists(references[i]));
            }
        }
    }

    /**
     * Å¥ÀÇ Å©±â°¡ Á¤È®ÇÏ°Ô À¯ÁöµÇ´ÂÁö Å×½ºÆ®ÇÑ´Ù. Å¥ÀÇ ³»¿ëÀ» º¯°æ½ÃÅ³ ¼ö ÀÖ´Â ¿ÀÆÛ·¹ÀÌ¼ÇÀ» ¸ðµÎ ¼öÇàÇÑ µÚ Å¥ÀÇ Å©±â°¡ ¿Ã¹Ù¸¥Áö È®ÀÎÇÑ´Ù.
     */
    public void testSize() throws Exception {
        QueueIterator it = queue.iterator();
        it.next();
        Assert.assertNotNull(it.getValue());
        Assert.assertNotNull(it.remove());
        Assert.assertNotNull(queue.push("TEST-SIZE"));
        Assert.assertNotNull(queue.remove(references[100]));
        Assert.assertEquals(ITEM_COUNT - 1, queue.size());
        db.close();
        db.open();
        queue = db.getQueue(QUEUE_NAME);

        Assert.assertEquals(ITEM_COUNT - 1, queue.size());
    }

    /**
     * {@link QueueImpl#clear()}¸¦ Å×½ºÆ®ÇÑ´Ù. Å¥ÀÇ ³»¿ëÀ» ÀüºÎ Áö¿î µÚ pop, front, remove, exists¸¦
     * È£ÃâÇØ Å¥ÀÇ ³»¿ëÀÌ Á¤¸» ºñ¾î ÀÖ´ÂÁö È®ÀÎÇÑ´Ù.
     */
    public void testClear() throws Exception {
        queue.clear();
        Assert.assertFalse(queue.iterator().next());
        Assert.assertNull(queue.remove(references[100]));
        Assert.assertFalse(queue.exists(references[100]));
        Assert.assertEquals(0, queue.size());
        Assert.assertTrue(queue.isEmpty());
    }

    /**
     * °ú°Å¿¡ È¹µæÇßÀ¸³ª ÇöÀç´Â »èÁ¦µÈ extent¿¡ ¼ÓÇÏ´Â {@link net.gleamynode.oil.impl.wal.QueueReferenceImpl}·Î
     * ÇöÀç °°Àº id¸¦ °®´Â extent¿¡ Á¢±ÙÀÌ ºÒ°¡´ÉÇÔÀ» È®ÀÎÇÑ´Ù.
     */
    public void testReferenceValidity() throws Exception {
        queue.clear();

        QueueReference ref = queue.push("TEST-REFERENCE-VALIDITY");
        Assert.assertFalse(queue.exists(references[0]));
        Assert.assertTrue(queue.exists(ref));
        Assert.assertNull(queue.get(references[0]));
        Assert.assertNotNull(queue.get(ref));
        Assert.assertNull(queue.remove(references[0]));
        Assert.assertNotNull(queue.remove(ref));
    }

    public void testMove() throws Exception {
        Queue targetQueue = db.getQueue("target");
        QueueIterator it = queue.iterator();
        int i = queue.size();

        while (it.next()) {
            Assert.assertNotNull(it.moveTo(targetQueue));
            i--;
        }

        Assert.assertEquals(i, queue.size());

        Assert.assertEquals(0, queue.size());
        Assert.assertTrue(queue.isEmpty());
        Assert.assertEquals(ITEM_COUNT, targetQueue.size());

        db.close();
        db.open();

        queue = db.getQueue(QUEUE_NAME);
        targetQueue = db.getQueue("target");

        Assert.assertEquals(0, queue.size());
        Assert.assertEquals(ITEM_COUNT, targetQueue.size());
    }

    public void testEmulatedMove() throws Exception {
        Queue targetQueue = db.getQueue("target");
        QueueIterator it = queue.iterator();
        int i = queue.size();

        while (it.next()) {
            Object value = it.getValue();
            it.remove();
            targetQueue.push(value);
            i--;
        }

        Assert.assertEquals(i, queue.size());

        Assert.assertEquals(0, queue.size());
        Assert.assertTrue(queue.isEmpty());
        Assert.assertEquals(ITEM_COUNT, targetQueue.size());

        db.close();
        db.open();

        queue = db.getQueue(QUEUE_NAME);
        targetQueue = db.getQueue("target");

        Assert.assertEquals(0, queue.size());
        Assert.assertEquals(ITEM_COUNT, targetQueue.size());
    }
}
