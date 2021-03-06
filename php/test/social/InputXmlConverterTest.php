<?php
/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * InputXmlConverter test case.
 */
class InputXmlConverterTest extends PHPUnit_Framework_TestCase {
  
  /**
   * @var InputXmlConverter
   */
  private $inputXmlConverter;

  /**
   * Prepares the environment before running a test.
   */
  protected function setUp() {
    parent::setUp();
    $this->inputXmlConverter = new InputXmlConverter(/* parameters */);
  }

  /**
   * Cleans up the environment after running a test.
   */
  protected function tearDown() {
    $this->inputXmlConverter = null;
    parent::tearDown();
  }

  /**
   * Tests InputXmlConverter->convertActivities()
   */
  public function testConvertActivities() {
    $xml = '<?xml version="1.0" encoding="UTF-8"?>
<response>
  <activity xmlns="http://ns.opensocial.org/2008/opensocial">
      <mediaItems>
        <MediaItem>
          <mimeType>IMAGE</mimeType>
          <type>image</type>
          <url>http://cdn.davesdaily.com/pictures/784-awesome-hands.jpg</url>
          <types>
            <AUDIO>audio</AUDIO>
            <VIDEO>video</VIDEO>
            <IMAGE>image</IMAGE>
          </types>
        </MediaItem>
      </mediaItems>
    <streamTitle>activities</streamTitle>
    <streamId>1</streamId>
    <userId>1</userId>
  </activity>
  <category term="status"/>
  <updated>2008-08-05T10:31:04+02:00</updated>
  <id>urn:guid:220</id>
  <title>example title</title>
  <summary>example summary</summary>
</response>
';
    $activity = $this->inputXmlConverter->convertActivities($xml);
    $this->assertEquals('urn:guid:220', $activity['id']);
    $this->assertEquals('example title', $activity['title']);
    $this->assertEquals('example summary', $activity['body']);
    $this->assertEquals('1', $activity['streamId']);
    $this->assertEquals('activities', $activity['streamTitle']);
    $this->assertEquals('2008-08-05T10:31:04+02:00', $activity['updated']);
    $this->assertEquals('image', $activity['mediaItems'][0]['type']);
    $this->assertEquals('IMAGE', $activity['mediaItems'][0]['mimeType']);
    $this->assertEquals('http://cdn.davesdaily.com/pictures/784-awesome-hands.jpg', $activity['mediaItems'][0]['url']);
  }

  /**
   * Tests inputXmlConverter->convertAppData()
   */
  public function testConvertAppData() {
    $xml = '<?xml version="1.0" encoding="UTF-8"?>
<response>
  <entry>
    <key>sign</key>
    <value>Virgo</value>
  </entry>
</response>';
    $appdata = $this->inputXmlConverter->convertAppData($xml);
    $expect = array('sign' => 'Virgo');
    $this->assertEquals($expect, $appdata);
  }

  /**
   * Tests inputXmlConverter->convertMessages()
   */
  public function testConvertMessages() {
    $xml = '<?xml version="1.0" encoding="UTF-8"?>
<response xmlns:osapi="http://opensocial.org/2008/opensocialapi">
  <osapi:recipient>example.org:AD38B3886625AAF</osapi:recipient>
  <osapi:recipient>example.org:997638BAA6F25AD</osapi:recipient>
  <title>You have an invitation from Joe</title>
  <id>{msgid}</id>
  <body>Click &lt;a href="http://app.example.org/invites/{msgid}"&gt;here&lt;/a&gt; to review your invitation.</body>
</response>';
    $message = $this->inputXmlConverter->convertMessages($xml);
    $this->assertEquals('{msgid}', $message['id']);
    $this->assertEquals('You have an invitation from Joe', $message['title']);
    $this->assertEquals('Click <a href="http://app.example.org/invites/{msgid}">here</a> to review your invitation.', $message['body']);
    $this->assertEquals('example.org:AD38B3886625AAF', $message['recipients'][0]);
    $this->assertEquals('example.org:997638BAA6F25AD', $message['recipients'][1]);
  }

  /**
   * Tests inputXmlConverter->convertPeople()
   */
  public function testConvertPeople() {
    $this->setExpectedException('Exception');
    $this->inputXmlConverter->convertPeople('');
  }
  
  public function testConvertAlbums() {
    $xml = '<?xml version="1.0" encoding="UTF-8"?>
            <album xmlns="http://ns.opensocial.org/2008/opensocial">
            <id>44332211</id>
            <thumbnailUrl>http://pages.example.org/albums/4433221-tn.png</thumbnailUrl>
            <caption>Example Album</caption>
            <description>This is an example album, and this text is an example description</description>
            <location>
               <latitude>0</latitude>
               <longitude>0</longitude>
            </location>
            <ownerId>example.org:55443322</ownerId>
            </album>';
    $album = $this->inputXmlConverter->convertAlbums($xml);
    $this->assertEquals('44332211', $album['id']);
    $this->assertEquals('http://pages.example.org/albums/4433221-tn.png', $album['thumbnailUrl']);
    $this->assertEquals('This is an example album, and this text is an example description', $album['description']);
    $this->assertEquals('Example Album', $album['title']);
    $this->assertEquals('example.org:55443322', $album['ownerId']);
    $this->assertFalse(empty($album['location']));
    $this->assertEquals(0, $album['location']['latitude']);
    $this->assertEquals(0, $album['location']['longitude']);
  }
  
  public function testConvertMediaItems() {
    $xml = '<?xml version="1.0" encoding="UTF-8"?>
            <mediaItem xmlns="http://ns.opensocial.org/2008/opensocial">
              <id>11223344</id>
              <thumbnailUrl>http://pages.example.org/images/11223344-tn.png</thumbnailUrl>
              <mimeType>image/png</mimeType>
              <type>image</type>
              <url>http://pages.example.org/images/11223344.png</url>
              <albumId>44332211</albumId>
            </mediaItem>';
    $mediaItem = $this->inputXmlConverter->convertMediaItems($xml);
    $this->assertEquals('11223344', $mediaItem['id']);
    $this->assertEquals('http://pages.example.org/images/11223344-tn.png', $mediaItem['thumbnailUrl']);
    $this->assertEquals('44332211', $mediaItem['albumId']);
    $this->assertEquals('http://pages.example.org/images/11223344.png', $mediaItem['url']);
    $this->assertEquals('image/png', $mediaItem['mimeType']);
  }
}