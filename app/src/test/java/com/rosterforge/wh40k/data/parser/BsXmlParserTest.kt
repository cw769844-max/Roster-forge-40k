package com.rosterforge.wh40k.data.parser

import com.google.common.truth.Truth.assertThat
import com.rosterforge.wh40k.domain.model.BattlefieldRole
import com.rosterforge.wh40k.domain.model.WeaponType
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BsXmlParserTest {

    private val parser = BsXmlParser()

    // ─────────────────────── parseCatalogue ───────────────────────

    @Test
    fun `parseCatalogue extracts faction id and name`() {
        val xml = catalogue(id = "sm", name = "Adeptus Astartes") { "" }
        val result = parser.parseCatalogue(xml.byteInputStream())
        assertThat(result.faction).isNotNull()
        assertThat(result.faction!!.id).isEqualTo("sm")
        assertThat(result.faction!!.name).isEqualTo("Adeptus Astartes")
        assertThat(result.faction!!.factionKeyword).isEqualTo("ADEPTUS ASTARTES")
    }

    @Test
    fun `parseCatalogue extracts a unit with stats and weapons`() {
        val xml = catalogue(id = "sm", name = "Adeptus Astartes") {
            """
            <selectionEntries>
              <selectionEntry id="captain" name="Captain" type="unit">
                <categoryLinks>
                  <categoryLink targetId="x" name="Character"/>
                </categoryLinks>
                <profiles>
                  <profile typeName="Unit" name="Captain">
                    <characteristics>
                      <characteristic name="M">6&quot;</characteristic>
                      <characteristic name="T">4</characteristic>
                      <characteristic name="Sv">3+</characteristic>
                      <characteristic name="W">5</characteristic>
                      <characteristic name="Ld">6+</characteristic>
                      <characteristic name="OC">1</characteristic>
                    </characteristics>
                  </profile>
                  <profile typeName="Ranged Weapons" name="Bolt rifle">
                    <characteristics>
                      <characteristic name="Range">24"</characteristic>
                      <characteristic name="A">2</characteristic>
                      <characteristic name="BS">3+</characteristic>
                      <characteristic name="S">4</characteristic>
                      <characteristic name="AP">-1</characteristic>
                      <characteristic name="D">1</characteristic>
                      <characteristic name="Keywords">Assault</characteristic>
                    </characteristics>
                  </profile>
                </profiles>
                <costs>
                  <cost name="pts" typeId="x" value="80"/>
                </costs>
              </selectionEntry>
            </selectionEntries>
            """
        }
        val result = parser.parseCatalogue(xml.byteInputStream())
        assertThat(result.units).hasSize(1)
        val captain = result.units.single()
        assertThat(captain.name).isEqualTo("Captain")
        assertThat(captain.role).isEqualTo(BattlefieldRole.CHARACTER)
        assertThat(captain.stats.movement).isEqualTo("6\"")
        assertThat(captain.stats.toughness).isEqualTo(4)
        assertThat(captain.stats.save).isEqualTo("3+")
        assertThat(captain.stats.wounds).isEqualTo(5)
        assertThat(captain.stats.objectiveControl).isEqualTo(1)
        assertThat(captain.weapons).hasSize(1)
        assertThat(captain.weapons.single().type).isEqualTo(WeaponType.RANGED)
        assertThat(captain.weapons.single().strength).isEqualTo("4")
        assertThat(captain.pointsCosts.single().points).isEqualTo(80)
    }

    @Test
    fun `parseCatalogue marks Epic Hero as named character with limit 1`() {
        val xml = catalogue(id = "sm", name = "Adeptus Astartes") {
            """
            <selectionEntries>
              <selectionEntry id="calgar" name="Marneus Calgar" type="unit">
                <categoryLinks>
                  <categoryLink targetId="x" name="Epic Hero"/>
                  <categoryLink targetId="y" name="Character"/>
                </categoryLinks>
                <profiles>
                  <profile typeName="Unit" name="Calgar">
                    <characteristics>
                      <characteristic name="M">6"</characteristic>
                      <characteristic name="T">6</characteristic>
                      <characteristic name="Sv">2+</characteristic>
                      <characteristic name="W">6</characteristic>
                      <characteristic name="Ld">6+</characteristic>
                      <characteristic name="OC">2</characteristic>
                    </characteristics>
                  </profile>
                </profiles>
                <costs><cost name="pts" typeId="x" value="185"/></costs>
              </selectionEntry>
            </selectionEntries>
            """
        }
        val result = parser.parseCatalogue(xml.byteInputStream())
        val unit = result.units.single()
        assertThat(unit.isNamedCharacter).isTrue()
        assertThat(unit.maxPerRoster).isEqualTo(1)
        assertThat(unit.role).isEqualTo(BattlefieldRole.EPIC_HERO)
    }

    // ─────────────────────── parseGameSystem ───────────────────────

    @Test
    fun `parseGameSystem extracts core stratagems`() {
        val xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gameSystem id="gs" name="WH40K" battleScribeVersion="2.03">
              <sharedSelectionEntries>
                <selectionEntry id="cp1" name="Command Re-roll" type="upgrade">
                  <categoryLinks>
                    <categoryLink targetId="x" name="Stratagem"/>
                  </categoryLinks>
                  <profiles>
                    <profile typeName="Stratagem" name="Command Re-roll">
                      <characteristics>
                        <characteristic name="When">Any phase</characteristic>
                        <characteristic name="Target">One unit</characteristic>
                        <characteristic name="Effect">Re-roll one die</characteristic>
                        <characteristic name="Type">Strategic Ploy</characteristic>
                      </characteristics>
                    </profile>
                  </profiles>
                  <costs><cost name="CP" typeId="x" value="1"/></costs>
                </selectionEntry>
                <selectionEntry id="ib" name="Insane Bravery" type="upgrade">
                  <categoryLinks>
                    <categoryLink targetId="x" name="Stratagem"/>
                  </categoryLinks>
                  <profiles>
                    <profile typeName="Stratagem" name="Insane Bravery">
                      <characteristics>
                        <characteristic name="When">Command phase</characteristic>
                        <characteristic name="Effect">Auto-pass test</characteristic>
                      </characteristics>
                    </profile>
                  </profiles>
                  <costs><cost name="CP" typeId="x" value="1"/></costs>
                </selectionEntry>
              </sharedSelectionEntries>
            </gameSystem>
        """.trimIndent()

        val result = parser.parseGameSystem(xml.byteInputStream())
        assertThat(result.coreStratagems.map { it.name })
            .containsExactly("Command Re-roll", "Insane Bravery")
        assertThat(result.coreStratagems.first().factionId).isNull()
        assertThat(result.coreStratagems.first().detachmentId).isNull()
    }

    // ─────────────────────── parseRelease ───────────────────────

    @Test
    fun `parseRelease dedupes by id across catalogues`() {
        val gst = """
            <?xml version="1.0"?>
            <gameSystem id="gs" name="WH40K"/>
        """.trimIndent()
        val cat1 = catalogue(id = "sm", name = "Space Marines") {
            """
            <selectionEntries>
              <selectionEntry id="dup-unit" name="Captain" type="unit">
                <categoryLinks><categoryLink targetId="x" name="Character"/></categoryLinks>
                <profiles>
                  <profile typeName="Unit" name="Captain">
                    <characteristics>
                      <characteristic name="M">6"</characteristic>
                      <characteristic name="T">4</characteristic>
                      <characteristic name="Sv">3+</characteristic>
                      <characteristic name="W">5</characteristic>
                      <characteristic name="Ld">6+</characteristic>
                      <characteristic name="OC">1</characteristic>
                    </characteristics>
                  </profile>
                </profiles>
                <costs><cost name="pts" typeId="x" value="80"/></costs>
              </selectionEntry>
            </selectionEntries>
            """
        }
        val cat2 = catalogue(id = "ba", name = "Blood Angels") {
            """
            <selectionEntries>
              <selectionEntry id="dup-unit" name="Captain" type="unit">
                <categoryLinks><categoryLink targetId="x" name="Character"/></categoryLinks>
                <profiles>
                  <profile typeName="Unit" name="Captain">
                    <characteristics>
                      <characteristic name="M">6"</characteristic>
                      <characteristic name="T">4</characteristic>
                      <characteristic name="Sv">3+</characteristic>
                      <characteristic name="W">5</characteristic>
                      <characteristic name="Ld">6+</characteristic>
                      <characteristic name="OC">1</characteristic>
                    </characteristics>
                  </profile>
                </profiles>
                <costs><cost name="pts" typeId="x" value="80"/></costs>
              </selectionEntry>
            </selectionEntries>
            """
        }

        val zipBytes = makeZip(
            "wh40k.gst" to gst,
            "sm.cat" to cat1,
            "ba.cat" to cat2,
        )

        val result = parser.parseRelease(zipBytes.inputStream())
        // Both catalogues have the captain, but only one entry survives in the result.
        assertThat(result.units).hasSize(1)
        assertThat(result.units.single().id).isEqualTo("dup-unit")
        assertThat(result.factions.map { it.id }).containsExactly("sm", "ba")
    }

    // ─────────────────────── helpers ───────────────────────

    private fun catalogue(id: String, name: String, body: () -> String): String =
        """
        <?xml version="1.0" encoding="UTF-8"?>
        <catalogue id="$id" name="$name" battleScribeVersion="2.03"
                   gameSystemId="gs" gameSystemRevision="1">
          ${body()}
        </catalogue>
        """.trimIndent()

    private fun makeZip(vararg entries: Pair<String, String>): ByteArray {
        val bos = ByteArrayOutputStream()
        ZipOutputStream(bos).use { zos ->
            for ((entryName, content) in entries) {
                zos.putNextEntry(ZipEntry(entryName))
                zos.write(content.toByteArray(Charsets.UTF_8))
                zos.closeEntry()
            }
        }
        return bos.toByteArray()
    }
}
