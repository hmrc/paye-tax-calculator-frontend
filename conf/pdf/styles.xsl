<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:attribute-set name="p">
        <xsl:attribute name="font-size">10.5pt</xsl:attribute>
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="padding-before">6px</xsl:attribute>
        <xsl:attribute name="padding-after">6px</xsl:attribute>
        <xsl:attribute name="role">P</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="small">
        <xsl:attribute name="font-size">9.5pt</xsl:attribute>
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="padding-before">6px</xsl:attribute>
        <xsl:attribute name="padding-after">6px</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="small-list">
        <xsl:attribute name="font-size">9.5pt</xsl:attribute>
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="line-height">11pt</xsl:attribute>
        <xsl:attribute name="padding-before">2.8px</xsl:attribute>
        <xsl:attribute name="padding-after">2.8px</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="normal-list">
        <xsl:attribute name="font-size">10.5pt</xsl:attribute>
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="line-height">12pt</xsl:attribute>
        <xsl:attribute name="padding-before">3px</xsl:attribute>
        <xsl:attribute name="padding-after">3px</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="address-line">
        <xsl:attribute name="font-size">10.5pt</xsl:attribute>
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="line-height">12pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="list-block">
        <xsl:attribute name="provisional-distance-between-starts">0.3cm</xsl:attribute>
        <xsl:attribute name="role">L</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="large">
        <xsl:attribute name="font-size">16pt</xsl:attribute>
        <xsl:attribute name="padding-before">6px</xsl:attribute>
        <xsl:attribute name="padding-after">6px</xsl:attribute>
        <xsl:attribute name="role">P</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="header-small">
        <xsl:attribute name="font-size">10.5pt</xsl:attribute>
        <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="padding-before">8px</xsl:attribute>
        <xsl:attribute name="padding-after">6px</xsl:attribute>
        <xsl:attribute name="role">H2</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="default-font-and-padding">
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="padding-before">6px</xsl:attribute>
        <xsl:attribute name="padding-after">6px</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="default-font-and-padding-bold">
        <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
        <xsl:attribute name="padding-before">6px</xsl:attribute>
        <xsl:attribute name="padding-after">6px</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="default-font">
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="default-font-bold">
        <xsl:attribute name="font-family">OpenSans-Bold</xsl:attribute>
        <xsl:attribute name="font-weight">bold</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="footer">
        <xsl:attribute name="font-size">9pt</xsl:attribute>
        <xsl:attribute name="font-family">OpenSans</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="line-height">14pt</xsl:attribute>
    </xsl:attribute-set>

</xsl:stylesheet>