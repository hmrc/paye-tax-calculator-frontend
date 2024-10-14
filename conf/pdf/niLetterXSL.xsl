<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:fox="http://xmlgraphics.apache.org/fop/extensions"
                xmlns:scala="java:util.XSLScalaBridge">

    <xsl:include href="pdf/styles.xsl"/>

    <xsl:param name="translator" />



    <xsl:output method="xml" indent="yes"/>
    <xsl:template match="/">
        <fo:root>
            <xsl:attribute name="xml:lang">
                <xsl:value-of select="scala:getLang($translator)"/>
            </xsl:attribute>
            <fo:layout-master-set>
                <!-- layout for the first page -->
                <fo:simple-page-master master-name="first"
                                       page-height="29.7cm"
                                       page-width="21cm"
                                       margin-top="0.5cm"
                                       margin-bottom="0.5cm"
                                       margin-left="2.2cm"
                                       margin-right="2.2cm">
                    <fo:region-body margin-top="0.7cm"/>
                    <fo:region-before extent="0.5cm"/>
                    <fo:region-after extent="0.3cm"/>
                </fo:simple-page-master>

                <!-- layout for the other pages -->
                <fo:simple-page-master master-name="rest"
                                       page-height="29.7cm"
                                       page-width="21cm"
                                       margin-top="0.5cm"
                                       margin-bottom="0.5cm"
                                       margin-left="2.2cm"
                                       margin-right="2.2cm">
                    <fo:region-body margin-top="0.5cm"/>
                    <fo:region-before extent="0.5cm"/>
                    <fo:region-after extent="0.3cm"/>
                </fo:simple-page-master>

                <fo:page-sequence-master master-name="basicPSM">
                    <fo:repeatable-page-master-alternatives>
                        <fo:conditional-page-master-reference master-reference="first"
                                                              page-position="first"/>
                        <fo:conditional-page-master-reference master-reference="rest"
                                                              page-position="rest"/>
                        <!-- recommended fallback procedure -->
                        <fo:conditional-page-master-reference master-reference="rest"/>
                    </fo:repeatable-page-master-alternatives>
                </fo:page-sequence-master>
            </fo:layout-master-set>
            <!-- end: defines page layout -->

            <!-- Document metadata -->
            <fo:declarations>
                <x:xmpmeta xmlns:x="adobe:ns:meta/">
                    <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
                        <rdf:Description xmlns:dc="http://purl.org/dc/elements/1.1/">
                            <dc:title>
                                <xsl:value-of select="scala:getMessagesText($translator, 'label.your_national_insurance_number_letter')"/>
                            </dc:title>
                        </rdf:Description>
                    </rdf:RDF>
                </x:xmpmeta>
            </fo:declarations>


            <!-- actual layout -->
            <fo:page-sequence master-reference="basicPSM">
                <!-- footer -->
                <fo:static-content role="artifact" flow-name="xsl-region-after">
                    <fo:block xsl:use-attribute-sets="footer">
                        <!-- date -->
                        <fo:block text-align="end">
                            <xsl:variable name="date" >
                                <xsl:value-of select="root/date"/>
                            </xsl:variable>
                            <xsl:value-of select="scala:getMessagesTextWithParameter($translator, 'label.hmrc_date', $date)"/>
                        </fo:block>
                    </fo:block>


                </fo:static-content>
                <!-- body -->
                <fo:flow flow-name="xsl-region-body">

                    <!-- variable for start of a link -->
                    <xsl:variable name="https" select="'https://'"/>

                    <xsl:variable name="language">
                        <xsl:value-of select="scala:getLang($translator)"/>
                    </xsl:variable>

                    <!-- logo and heading -->
                    <fo:block role="Div" space-after="10px">
                        <fo:inline-container role="Div" inline-progression-dimension="22%">
                            <fo:block role="Div"
                                      border-left-style="solid"
                                      border-width="2px"
                                      border-color="#28a197"
                                      padding-start="4px">
                                <fo:wrapper role="artifact">
                                    <fo:external-graphic content-type="content-type:image/png" src="pdf/logo/hmrc-logo.jpg" content-height="scale-to-fit"  content-width="0.8cm"/>
                                </fo:wrapper>
                                <fo:block role="P"
                                          xsl:use-attribute-sets="default-font"
                                          line-height="14pt"
                                          font-size="14pt"
                                          linefeed-treatment="preserve">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.hm_revenue_customs_ni_print.revenue.pdf')"/>
                                    <xsl:text>&#xA;</xsl:text>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.hm_revenue_customs_ni_print.customs.pdf')"/>
                                </fo:block>
                            </fo:block>
                        </fo:inline-container>
                        <fo:inline-container role="Div" inline-progression-dimension="78%">
                            <fo:block role="H1"
                                      text-align="end"
                                      padding-before="10px"
                                      padding-after="6px"
                                      xsl:use-attribute-sets="default-font-bold">
                                <xsl:value-of select="scala:getMessagesText($translator, 'label.your_national_insurance_number')"/>
                            </fo:block>
                        </fo:inline-container>
                    </fo:block>


                    <!-- Addresses -->
                    <fo:block role="Div">
                        <fo:inline-container role="Div" inline-progression-dimension="63%">
                            <fo:block>
                                <fo:block role="P" xsl:use-attribute-sets="address-line">
                                    <xsl:value-of select="root/initials-name"/>
                                </fo:block>
                                <xsl:for-each select="root/address/address-line">
                                    <fo:block role="P" xsl:use-attribute-sets="address-line">
                                        <xsl:value-of select="."/>
                                    </fo:block>
                                </xsl:for-each>
                                <fo:block role="P" xsl:use-attribute-sets="address-line">
                                    <xsl:value-of select="root/postcode"/>
                                </fo:block>
                            </fo:block>
                        </fo:inline-container>
                        <fo:inline-container role="Div" inline-progression-dimension="37%">
                            <fo:block>
                                <fo:block role="P" xsl:use-attribute-sets="address-line">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.nic_eo_hmrc_address.line1.pdf')"/>
                                </fo:block>
                                <fo:block role="P" xsl:use-attribute-sets="address-line">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.nic_eo_hmrc_address.line2')"/>
                                </fo:block>
                                <xsl:if test="$language = 'cy'">
                                    <fo:block role="P" xsl:use-attribute-sets="address-line">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.nic_eo_hmrc_address.line3')"/>
                                    </fo:block>
                                </xsl:if>
                                <fo:block role="P" xsl:use-attribute-sets="address-line">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.nic_eo_hmrc_address.postcode')"/>
                                </fo:block>
                                <fo:block role="P" xsl:use-attribute-sets="p">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.phone_text')"/>
                                    <xsl:text>&#x9;</xsl:text>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.contact_number')"/>
                                </fo:block>
                                <fo:block role="P" xsl:use-attribute-sets="p">
                                    <xsl:variable name="hmrc-link">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.www_gov_uk_hmrc')"/>
                                    </xsl:variable>
                                    <fo:basic-link color="#1F70B8"
                                                   text-decoration="underline">
                                        <xsl:attribute name="external-destination">
                                            <xsl:value-of select="concat($https, $hmrc-link)"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="fox:alt-text">
                                            <xsl:value-of select="scala:getMessagesText($translator, 'label.www_gov_uk_hmrc.alt')"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="$hmrc-link"/>
                                    </fo:basic-link>
                                </fo:block>
                            </fo:block>
                        </fo:inline-container>
                    </fo:block>


                    <!-- name and title -->
                    <fo:block xsl:use-attribute-sets="p">
                        <xsl:value-of select="root/full-name"/>
                    </fo:block>


                    <!-- NINO number box -->
                    <fo:block role="Div"
                              space-after="10px"
                              background-color="#DAF4F2"
                              border-style="solid"
                              border-width="1.5px"
                              border-color="#28a197">
                        <fo:block role="H2" xsl:use-attribute-sets="header-small"
                                  text-align="center">
                            <xsl:value-of select="scala:getMessagesText($translator, 'label.your_national_insurance_number_is')"/>
                        </fo:block>
                        <fo:block role="P"
                                  font-size="20pt"
                                  text-align="center"
                                  xsl:use-attribute-sets="default-font-bold">
                            <xsl:value-of select="root/nino"/>
                        </fo:block>
                    </fo:block>

                    <fo:block role="P"
                              space-after="10px"
                              text-align="center"
                              xsl:use-attribute-sets="default-font-and-padding-bold">
                        <xsl:value-of select="scala:getMessagesText($translator, 'label.keep_this_number_in_a_safe_place_do_not_destroy_this_letter')"/>
                    </fo:block>

                    <!-- about NINO information -->
                    <fo:block role="Div">
                        <fo:inline-container role="Div" inline-progression-dimension="63%">
                            <fo:block role="Div"
                                      margin-left="5px"
                                      margin-right="5px">
                                <fo:block role="H3"
                                          xsl:use-attribute-sets="header-small">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.about_your_national_insurance_number')"/>
                                </fo:block>

                                <fo:block xsl:use-attribute-sets="p">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.your_national_insurance_number_is_unique_to_you_and_will_never_change_')"/>
                                </fo:block>

                                <fo:block xsl:use-attribute-sets="p">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.you_will_need_it_if_you')"/>
                                </fo:block>
                                <fo:list-block xsl:use-attribute-sets="list-block">
                                    <fo:list-item xsl:use-attribute-sets="normal-list" role="LI">
                                        <fo:list-item-label role="Lbl" end-indent="1em">
                                            <fo:block>
                                                <fo:inline>
                                                    <fo:wrapper role="artifact">
                                                        &#8226;
                                                    </fo:wrapper>
                                                </fo:inline>
                                            </fo:block>
                                        </fo:list-item-label>
                                        <fo:list-item-body role="LBody" start-indent="2em">
                                            <fo:block>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'label.start_work_including_part_time_and_weekend_jobs')"/>
                                            </fo:block>
                                        </fo:list-item-body>
                                    </fo:list-item>
                                    <fo:list-item xsl:use-attribute-sets="normal-list" role="LI">
                                        <fo:list-item-label role="Lbl" end-indent="1em">
                                            <fo:block>
                                                <fo:inline>
                                                    <fo:wrapper role="artifact">
                                                        &#8226;
                                                    </fo:wrapper>
                                                </fo:inline>
                                            </fo:block>
                                        </fo:list-item-label>
                                        <fo:list-item-body role="LBody" start-indent="2em">
                                            <fo:block>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'label.apply_for_a_driving_licence')"/>
                                            </fo:block>
                                        </fo:list-item-body>
                                    </fo:list-item>
                                    <fo:list-item xsl:use-attribute-sets="normal-list" role="LI">
                                        <fo:list-item-label role="Lbl" end-indent="1em">
                                            <fo:block>
                                                <fo:inline>
                                                    <fo:wrapper role="artifact">
                                                        &#8226;
                                                    </fo:wrapper>
                                                </fo:inline>
                                            </fo:block>
                                        </fo:list-item-label>
                                        <fo:list-item-body role="LBody" start-indent="2em">
                                            <fo:block>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'label.apply_for_a_student_loan')"/>
                                            </fo:block>
                                        </fo:list-item-body>
                                    </fo:list-item>
                                    <fo:list-item xsl:use-attribute-sets="normal-list" role="LI">
                                        <fo:list-item-label role="Lbl" end-indent="1em">
                                            <fo:block>
                                                <fo:inline>
                                                    <fo:wrapper role="artifact">
                                                        &#8226;
                                                    </fo:wrapper>
                                                </fo:inline>
                                            </fo:block>
                                        </fo:list-item-label>
                                        <fo:list-item-body role="LBody" start-indent="2em">
                                            <fo:block>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'label.claim_state_benefits')"/>
                                            </fo:block>
                                        </fo:list-item-body>
                                    </fo:list-item>
                                    <fo:list-item xsl:use-attribute-sets="normal-list" role="LI">
                                        <fo:list-item-label role="Lbl" end-indent="1em">
                                            <fo:block>
                                                <fo:inline>
                                                    <fo:wrapper role="artifact">
                                                        &#8226;
                                                    </fo:wrapper>
                                                </fo:inline>
                                            </fo:block>
                                        </fo:list-item-label>
                                        <fo:list-item-body role="LBody" start-indent="2em">
                                            <fo:block>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'label.register_to_vote')"/>
                                            </fo:block>
                                        </fo:list-item-body>
                                    </fo:list-item>
                                </fo:list-block>
                                <fo:block xsl:use-attribute-sets="p">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.it_is_not_proof_of')"/>
                                </fo:block>
                                <fo:list-block xsl:use-attribute-sets="list-block">
                                    <fo:list-item xsl:use-attribute-sets="normal-list" role="LI">
                                        <fo:list-item-label role="Lbl" end-indent="1em">
                                            <fo:block>
                                                <fo:inline>
                                                    <fo:wrapper role="artifact">
                                                        &#8226;
                                                    </fo:wrapper>
                                                </fo:inline>
                                            </fo:block>
                                        </fo:list-item-label>
                                        <fo:list-item-body role="LBody" start-indent="2em">
                                            <fo:block>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'label.your_identity')"/>
                                            </fo:block>
                                        </fo:list-item-body>
                                    </fo:list-item>
                                    <fo:list-item xsl:use-attribute-sets="normal-list" role="LI">
                                        <fo:list-item-label role="Lbl" end-indent="1em">
                                            <fo:block>
                                                <fo:inline>
                                                    <fo:wrapper role="artifact">
                                                        &#8226;
                                                    </fo:wrapper>
                                                </fo:inline>
                                            </fo:block>
                                        </fo:list-item-label>
                                        <fo:list-item-body role="LBody" start-indent="2em">
                                            <fo:block>
                                                <xsl:value-of select="scala:getMessagesText($translator, 'label.your_right_to_work_in_the_uk')"/>
                                            </fo:block>
                                        </fo:list-item-body>
                                    </fo:list-item>
                                </fo:list-block>
                                <fo:block role="H3"
                                          xsl:use-attribute-sets="header-small">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.child.trust.fund')"/>
                                </fo:block>
                                <fo:block xsl:use-attribute-sets="p">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.child.trust.fund.details')"/>
                                    <xsl:text>&#160;</xsl:text>
                                    <xsl:variable name="child-trust-funds-link" >
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.child.trust.fund.details.link')"/>
                                    </xsl:variable>
                                    <fo:basic-link color="#1F70B8"
                                                   text-decoration="underline">
                                        <xsl:attribute name="external-destination">
                                            <xsl:value-of select="concat($https, $child-trust-funds-link)"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="fox:alt-text">
                                            <xsl:value-of select="$child-trust-funds-link"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="$child-trust-funds-link"/>
                                    </fo:basic-link>
                                </fo:block>

                                <!-- only display welsh language section when in English -->
                                <xsl:if test="$language = 'en'">
                                    <fo:block role="H3"
                                              xsl:use-attribute-sets="header-small">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.welsh_language')"/>
                                    </fo:block>
                                    <fo:block xsl:use-attribute-sets="p">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.to_continue_to_receive_a_welsh_language_service_')"/>
                                    </fo:block>
                                    <fo:list-block xsl:use-attribute-sets="list-block">
                                        <fo:list-item xsl:use-attribute-sets="normal-list" role="LI">
                                            <fo:list-item-label role="Lbl" end-indent="1em">
                                                <fo:block>
                                                    <fo:inline>
                                                        <fo:wrapper role="artifact">
                                                            &#8226;
                                                        </fo:wrapper>
                                                    </fo:inline>
                                                </fo:block>
                                            </fo:list-item-label>
                                            <fo:list-item-body role="LBody" start-indent="2em">
                                                <fo:block>
                                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.email')"/>
                                                    <xsl:text>&#160;</xsl:text>
                                                    <xsl:variable name="email-link">
                                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.email_for_welsh_language')"/>
                                                    </xsl:variable>
                                                    <fo:basic-link color="#1F70B8"
                                                                   text-decoration="underline">
                                                        <xsl:attribute name="external-destination">
                                                            <xsl:value-of select='concat("mailto:", $email-link)'/>
                                                        </xsl:attribute>
                                                        <xsl:attribute name="fox:alt-text">
                                                            <xsl:value-of select="$email-link"/>
                                                        </xsl:attribute>
                                                        <xsl:value-of select="$email-link"/>
                                                    </fo:basic-link>
                                                </fo:block>
                                            </fo:list-item-body>
                                        </fo:list-item>
                                        <fo:list-item xsl:use-attribute-sets="normal-list" role="LI">
                                            <fo:list-item-label role="Lbl" end-indent="1em">
                                                <fo:block>
                                                    <fo:inline>
                                                        <fo:wrapper role="artifact">
                                                            &#8226;
                                                        </fo:wrapper>
                                                    </fo:inline>
                                                </fo:block>
                                            </fo:list-item-label>
                                            <fo:list-item-body role="LBody" start-indent="2em">
                                                <fo:block>
                                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.phone.number.for.welsh.language')"/>
                                                </fo:block>
                                            </fo:list-item-body>
                                        </fo:list-item>
                                    </fo:list-block>
                                </xsl:if>

                            </fo:block>
                        </fo:inline-container>
                        <fo:inline-container role="Div" inline-progression-dimension="37%">
                            <fo:block role="Div"
                                      background-color="#DAF4F2"
                                      margin-left="5px"
                                      margin-right="5px"
                                      padding-start="5px"
                                      padding-end="5px"
                                      padding-before="2px"
                                      padding-after="5px">
                                <fo:block role="H3"
                                          border-after-style="solid"
                                          border-color="#00A298"
                                          border-width="1.5px"
                                          xsl:use-attribute-sets="header-small"
                                          linefeed-treatment="preserve">
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.now_you_have_got_your')"/>
                                    <xsl:text>&#xA;</xsl:text>
                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.national_insurance_number')"/>
                                </fo:block>
                                <fo:block role="Div"
                                          border-after-style="solid"
                                          border-width="1.5px"
                                          border-color="#00A298"
                                          padding-after="5px">
                                    <fo:block xsl:use-attribute-sets="small">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.you_can_download_and_use_the_hmrc_app_or_go_online_to.text.part1')"/>
                                        <xsl:text>&#160;</xsl:text>
                                        <xsl:variable name="personal-tax-account-link">
                                            <xsl:value-of select="scala:getMessagesText($translator, 'label.you_can_download_and_use_the_hmrc_app_or_go_online_to.link')"/>
                                        </xsl:variable>
                                        <fo:basic-link color="#1F70B8"
                                                       text-decoration="underline">
                                            <xsl:attribute name="external-destination">
                                                <xsl:value-of select="concat($https, $personal-tax-account-link)"/>
                                            </xsl:attribute>
                                            <xsl:attribute name="fox:alt-text">
                                                <xsl:value-of select="$personal-tax-account-link"/>
                                            </xsl:attribute>
                                            <xsl:value-of select="$personal-tax-account-link"/>
                                        </fo:basic-link>
                                        <xsl:text>&#160;</xsl:text>
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.you_can_download_and_use_the_hmrc_app_or_go_online_to.text.part2')"/>
                                    </fo:block>
                                    <fo:list-block xsl:use-attribute-sets="list-block">
                                        <fo:list-item xsl:use-attribute-sets="small-list" role="LI">
                                            <fo:list-item-label role="Lbl" end-indent="1em">
                                                <fo:block>
                                                    <fo:inline>
                                                        <fo:wrapper role="artifact">
                                                            &#8226;
                                                        </fo:wrapper>
                                                    </fo:inline>
                                                </fo:block>
                                            </fo:list-item-label>
                                            <fo:list-item-body role="LBody" start-indent="2em">
                                                <fo:block>
                                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.create_and_access_your_personal_tax_account')"/>
                                                </fo:block>
                                            </fo:list-item-body>
                                        </fo:list-item>
                                        <fo:list-item xsl:use-attribute-sets="small-list" role="LI">
                                            <fo:list-item-label role="Lbl" end-indent="1em">
                                                <fo:block>
                                                    <fo:inline>
                                                        <fo:wrapper role="artifact">
                                                            &#8226;
                                                        </fo:wrapper>
                                                    </fo:inline>
                                                </fo:block>
                                            </fo:list-item-label>
                                            <fo:list-item-body role="LBody" start-indent="2em">
                                                <fo:block>
                                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.save_and_print_another_copy_of_this_letter')"/>
                                                </fo:block>
                                            </fo:list-item-body>
                                        </fo:list-item>
                                        <fo:list-item xsl:use-attribute-sets="small-list" role="LI">
                                            <fo:list-item-label role="Lbl" end-indent="1em">
                                                <fo:block>
                                                    <fo:inline>
                                                        <fo:wrapper role="artifact">
                                                            &#8226;
                                                        </fo:wrapper>
                                                    </fo:inline>
                                                </fo:block>
                                            </fo:list-item-label>
                                            <fo:list-item-body role="LBody" start-indent="2em">
                                                <fo:block>
                                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.tell_us_about_a_change_to_your_address')"/>
                                                </fo:block>
                                            </fo:list-item-body>
                                        </fo:list-item>
                                        <fo:list-item xsl:use-attribute-sets="small-list" role="LI">
                                            <fo:list-item-label role="Lbl" end-indent="1em">
                                                <fo:block>
                                                    <fo:inline>
                                                        <fo:wrapper role="artifact">
                                                            &#8226;
                                                        </fo:wrapper>
                                                    </fo:inline>
                                                </fo:block>
                                            </fo:list-item-label>
                                            <fo:list-item-body role="LBody" start-indent="2em">
                                                <fo:block>
                                                    <xsl:value-of select="scala:getMessagesText($translator, 'label.view_your_tax_code')"/>
                                                </fo:block>
                                            </fo:list-item-body>
                                        </fo:list-item>
                                    </fo:list-block>
                                </fo:block>
                                <fo:block role="Div">
                                    <fo:block xsl:use-attribute-sets="small">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.view_more_information_about_national_insurance_at')"/>
                                    </fo:block>
                                    <xsl:variable name="national-insurance-link">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.www_gov_uk_national_insurance')"/>
                                    </xsl:variable>
                                    <fo:basic-link color="#1F70B8"
                                                   text-decoration="underline"
                                                   xsl:use-attribute-sets="small">
                                        <xsl:attribute name="external-destination">
                                            <xsl:value-of select="concat($https, $national-insurance-link)"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="fox:alt-text">
                                            <xsl:value-of select="$national-insurance-link"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="$national-insurance-link"/>
                                    </fo:basic-link>
                                    <fo:block xsl:use-attribute-sets="small">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.or_our_youtube_channel_at')"/>
                                    </fo:block>
                                    <xsl:variable name="youtube-channel-link">
                                        <xsl:value-of select="scala:getMessagesText($translator, 'label.www_youtube_com_hmrcgovuk')"/>
                                    </xsl:variable>
                                    <fo:basic-link color="#1F70B8"
                                                   text-decoration="underline"
                                                   xsl:use-attribute-sets="small">
                                        <xsl:attribute name="external-destination">
                                            <xsl:value-of select="concat($https, $youtube-channel-link)"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="fox:alt-text">
                                            <xsl:value-of select="$youtube-channel-link"/>
                                        </xsl:attribute>
                                        <xsl:value-of select="$youtube-channel-link"/>
                                    </fo:basic-link>
                                </fo:block>
                            </fo:block>
                        </fo:inline-container>
                    </fo:block>


                    <!-- end text -->
                    <fo:block role="Div" xsl:use-attribute-sets="default-font">
                        <fo:block xsl:use-attribute-sets="large">
                            <xsl:value-of select="scala:getMessagesText($translator, 'label.information_is_available_in_large_print_audio_tape_and_braille_formats')"/>
                        </fo:block>

                        <fo:block xsl:use-attribute-sets="large">
                            <xsl:value-of select="scala:getMessagesText($translator, 'label.text_relay_service_prefix_number_18001')"/>
                        </fo:block>
                    </fo:block>

                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>
</xsl:stylesheet>