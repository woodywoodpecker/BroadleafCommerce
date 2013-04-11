/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.core.offer.domain;

import org.broadleafcommerce.common.currency.util.BroadleafCurrencyUtils;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.persistence.ArchiveStatus;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.common.presentation.AdminPresentation;
import org.broadleafcommerce.common.presentation.AdminPresentationClass;
import org.broadleafcommerce.common.presentation.AdminPresentationMapField;
import org.broadleafcommerce.common.presentation.AdminPresentationMapFields;
import org.broadleafcommerce.common.presentation.AdminPresentationMapKey;
import org.broadleafcommerce.common.presentation.PopulateToOneFieldsEnum;
import org.broadleafcommerce.common.presentation.RequiredOverride;
import org.broadleafcommerce.common.presentation.client.SupportedFieldType;
import org.broadleafcommerce.common.presentation.client.VisibilityEnum;
import org.broadleafcommerce.common.util.DateUtil;
import org.broadleafcommerce.core.offer.service.type.OfferDeliveryType;
import org.broadleafcommerce.core.offer.service.type.OfferDiscountType;
import org.broadleafcommerce.core.offer.service.type.OfferItemRestrictionRuleType;
import org.broadleafcommerce.core.offer.service.type.OfferType;
import org.broadleafcommerce.openadmin.server.service.type.RuleIdentifier;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "BLC_OFFER")
@Inheritance(strategy=InheritanceType.JOINED)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
@AdminPresentationClass(populateToOneFields = PopulateToOneFieldsEnum.TRUE, friendlyName = "OfferImpl_baseOffer")
@SQLDelete(sql="UPDATE BLC_OFFER SET ARCHIVED = 'Y' WHERE OFFER_ID = ?")
public class OfferImpl implements Offer, Status {

    public static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator= "OfferId")
    @GenericGenerator(
        name="OfferId",
        strategy="org.broadleafcommerce.common.persistence.IdOverrideTableGenerator",
        parameters = {
            @Parameter(name="table_name", value="SEQUENCE_GENERATOR"),
            @Parameter(name="segment_column_name", value="ID_NAME"),
            @Parameter(name="value_column_name", value="ID_VAL"),
            @Parameter(name="segment_value", value="OfferImpl"),
            @Parameter(name="increment_size", value="50"),
            @Parameter(name="entity_name", value="org.broadleafcommerce.core.offer.domain.OfferImpl")
        }
    )
    @Column(name = "OFFER_ID")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Id", visibility = VisibilityEnum.HIDDEN_ALL)
    protected Long id;

    @Column(name = "OFFER_NAME", nullable=false)
    @Index(name="OFFER_NAME_INDEX", columnNames={"OFFER_NAME"})
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Name", order = 1000, 
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description,
        prominent = true, gridOrder = 1)
    protected String name;

    @Column(name = "OFFER_DESCRIPTION")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Description", order = 2000, 
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description,
        prominent = true, gridOrder = 2,
        largeEntry = true)
    protected String description;

    @Column(name = "MARKETING_MESSASGE")
    @Index(name = "OFFER_MARKETING_MESSAGE_INDEX", columnNames = { "MARKETING_MESSASGE" })
    @AdminPresentation(friendlyName = "OfferImpl_marketingMessage", order = 4000, 
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced)
    protected String marketingMessage;

    @Column(name = "OFFER_TYPE", nullable=false)
    @Index(name="OFFER_TYPE_INDEX", columnNames={"OFFER_TYPE"})
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Type", order = 3000, 
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description,
        prominent =  true, gridOrder = 3,
        fieldType=SupportedFieldType.BROADLEAF_ENUMERATION, 
        broadleafEnumeration="org.broadleafcommerce.core.offer.service.type.OfferType")
    protected String type;

    @Column(name = "OFFER_DISCOUNT_TYPE")
    @Index(name="OFFER_DISCOUNT_INDEX", columnNames={"OFFER_DISCOUNT_TYPE"})
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Discount_Type", order = 1000, 
        group = Presentation.Group.Name.Amount, groupOrder = Presentation.Group.Order.Amount,
        fieldType=SupportedFieldType.BROADLEAF_ENUMERATION, 
        broadleafEnumeration="org.broadleafcommerce.core.offer.service.type.OfferDiscountType")
    protected String discountType;

    @Column(name = "OFFER_VALUE", nullable=false, precision=19, scale=5)
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Value", order = 2000, 
        group = Presentation.Group.Name.Amount, groupOrder = Presentation.Group.Order.Amount,
        prominent = true, gridOrder = 4)
    protected BigDecimal value;

    @Column(name = "OFFER_PRIORITY")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Priority", order = 7, 
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced)
    protected int priority;

    @Column(name = "START_DATE")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Start_Date", order = 1,
        group = Presentation.Group.Name.ActivityRange, groupOrder = Presentation.Group.Order.ActivityRange)
    protected Date startDate;

    @Column(name = "END_DATE")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_End_Date", order = 2,
        group = Presentation.Group.Name.ActivityRange, groupOrder = Presentation.Group.Order.ActivityRange)
    protected Date endDate;

    @Column(name = "STACKABLE")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Stackable",
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced)
    protected boolean stackable;

    @Column(name = "TARGET_SYSTEM")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Target_System",
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced)
    protected String targetSystem;

    @Column(name = "APPLY_TO_SALE_PRICE")
    @AdminPresentation(friendlyName = "OfferImpl_Apply_To_Sale_Price", order = 3000,
        group = Presentation.Group.Name.Amount, groupOrder = Presentation.Group.Order.Amount)
    protected boolean applyToSalePrice;

    @Column(name = "APPLIES_TO_RULES")
    @AdminPresentation(excluded = true)
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Deprecated
    protected String appliesToOrderRules;

    @Column(name = "APPLIES_WHEN_RULES")
    @AdminPresentation(excluded = true)
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Deprecated
    protected String appliesToCustomerRules;

    @Column(name = "APPLY_OFFER_TO_MARKED_ITEMS")
    @AdminPresentation(excluded = true)
    @Deprecated
    protected boolean applyDiscountToMarkedItems;
    
    /**
     * No offers can be applied on top of this offer; 
     * If false, stackable has to be false also
     */
    @Column(name = "COMBINABLE_WITH_OTHER_OFFERS")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Combinable", 
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced,
        visibility = VisibilityEnum.HIDDEN_ALL)
    protected boolean combinableWithOtherOffers;  

    @Column(name = "OFFER_DELIVERY_TYPE", nullable=false)
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Delivery_Type", order = 5000,
        group = Presentation.Group.Name.Description, groupOrder = Presentation.Group.Order.Description,
        fieldType=SupportedFieldType.BROADLEAF_ENUMERATION, 
        broadleafEnumeration="org.broadleafcommerce.core.offer.service.type.OfferDeliveryType")
    @Index(name="OFFER_DELIVERY_INDEX", columnNames={"OFFER_DELIVERY_TYPE"})
    protected String deliveryType;

    @Column(name = "MAX_USES")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Max_Uses_Per_Order", order = 7,
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced)
    protected int maxUsesPerOrder;

    @Column(name = "MAX_USES_PER_CUSTOMER")
    @AdminPresentation(friendlyName = "OfferImpl_Max_Uses_Per_Customer", order = 8,
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced)
    protected Long maxUsesPerCustomer;

    @Column(name = "USES")
    @AdminPresentation(friendlyName = "OfferImpl_Offer_Current_Uses", 
        visibility = VisibilityEnum.HIDDEN_ALL)
    @Deprecated
    protected int uses;
    
    @Column(name = "OFFER_ITEM_QUALIFIER_RULE")
    @AdminPresentation(friendlyName = "OfferImpl_Item_Qualifier_Rule", 
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced,
        fieldType = SupportedFieldType.BROADLEAF_ENUMERATION, 
        broadleafEnumeration = "org.broadleafcommerce.core.offer.service.type.OfferItemRestrictionRuleType")
    protected String offerItemQualifierRuleType;
    
    @Column(name = "OFFER_ITEM_TARGET_RULE")
    @AdminPresentation(friendlyName = "OfferImpl_Item_Target_Rule", 
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced,
        fieldType = SupportedFieldType.BROADLEAF_ENUMERATION, 
        broadleafEnumeration = "org.broadleafcommerce.core.offer.service.type.OfferItemRestrictionRuleType")
    protected String offerItemTargetRuleType;
    
    @OneToMany(fetch = FetchType.LAZY, targetEntity = OfferItemCriteriaImpl.class, cascade={CascadeType.ALL})
    @JoinTable(name = "BLC_QUAL_CRIT_OFFER_XREF", joinColumns = @JoinColumn(name = "OFFER_ID"), 
        inverseJoinColumns = @JoinColumn(name = "OFFER_ITEM_CRITERIA_ID"))
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @AdminPresentation(friendlyName = "OfferImpl_Qualifying_Item_Rule",
        group = Presentation.Group.Name.Qualifiers, groupOrder = Presentation.Group.Order.Qualifiers,
        fieldType = SupportedFieldType.RULE_WITH_QUANTITY, ruleIdentifier = RuleIdentifier.ORDERITEM)
    protected Set<OfferItemCriteria> qualifyingItemCriteria = new HashSet<OfferItemCriteria>();
    
    @OneToMany(fetch = FetchType.LAZY, targetEntity = OfferItemCriteriaImpl.class, cascade={CascadeType.ALL})
    @JoinTable(name = "BLC_TAR_CRIT_OFFER_XREF", joinColumns = @JoinColumn(name = "OFFER_ID"), 
        inverseJoinColumns = @JoinColumn(name = "OFFER_ITEM_CRITERIA_ID"))
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @AdminPresentation(friendlyName = "OfferImpl_Target_Item_Rule",
        group = Presentation.Group.Name.ItemTarget, groupOrder = Presentation.Group.Order.ItemTarget,
        fieldType = SupportedFieldType.RULE_WITH_QUANTITY, 
        ruleIdentifier = RuleIdentifier.ORDERITEM, 
        requiredOverride = RequiredOverride.REQUIRED)
    protected Set<OfferItemCriteria> targetItemCriteria = new HashSet<OfferItemCriteria>();
    
    @Column(name = "TOTALITARIAN_OFFER")
    @AdminPresentation(friendlyName = "OfferImpl_Totalitarian_Offer", 
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced,
        visibility = VisibilityEnum.HIDDEN_ALL)
    protected Boolean totalitarianOffer;
    
    @ManyToMany(targetEntity = OfferRuleImpl.class, cascade = {CascadeType.ALL})
    @JoinTable(name = "BLC_OFFER_RULE_MAP", 
        inverseJoinColumns = @JoinColumn(name = "OFFER_RULE_ID", referencedColumnName = "OFFER_RULE_ID"))
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @MapKeyColumn(name = "MAP_KEY", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blStandardElements")
    @AdminPresentationMapFields(
        mapDisplayFields = {
            @AdminPresentationMapField(
                fieldName = @AdminPresentationMapKey(keyName = RuleIdentifier.CUSTOMER_FIELD_KEY, 
                    friendlyKeyName = "OfferImpl_Customer_Rule"),
                fieldPresentation = @AdminPresentation(fieldType = SupportedFieldType.RULE_SIMPLE, 
                    group = Presentation.Group.Name.Qualifiers, groupOrder = Presentation.Group.Order.Qualifiers,
                    ruleIdentifier = RuleIdentifier.CUSTOMER)
            ),
            @AdminPresentationMapField(
                fieldName = @AdminPresentationMapKey(keyName = RuleIdentifier.ORDER_FIELD_KEY, 
                    friendlyKeyName = "OfferImpl_Order_Rule"),
                fieldPresentation = @AdminPresentation(fieldType = SupportedFieldType.RULE_SIMPLE, 
                    group = Presentation.Group.Name.Qualifiers, groupOrder = Presentation.Group.Order.Qualifiers,
                    ruleIdentifier = RuleIdentifier.ORDER)
            ),
            @AdminPresentationMapField(
                fieldName = @AdminPresentationMapKey(keyName = RuleIdentifier.FULFILLMENT_GROUP_FIELD_KEY, 
                    friendlyKeyName = "OfferImpl_FG_Rule"),
                fieldPresentation = @AdminPresentation(fieldType = SupportedFieldType.RULE_SIMPLE, 
                    group = Presentation.Group.Name.Qualifiers, groupOrder = Presentation.Group.Order.Qualifiers,
                    ruleIdentifier = RuleIdentifier.FULFILLMENTGROUP)
            )
        }
    )
    Map<String, OfferRule> offerMatchRules = new HashMap<String, OfferRule>();
    
    @Column(name = "USE_NEW_FORMAT")
    @AdminPresentation(friendlyName = "OfferImpl_Treat_As_New_Format",
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced,
        visibility = VisibilityEnum.HIDDEN_ALL)
    protected Boolean treatAsNewFormat;
    
    @Column(name = "QUALIFYING_ITEM_MIN_TOTAL", precision=19, scale=5)
    @AdminPresentation(friendlyName="OfferImpl_Qualifying_Item_Subtotal",
        tab = Presentation.Tab.Name.Advanced, tabOrder = Presentation.Tab.Order.Advanced,
        group = Presentation.Group.Name.Advanced, groupOrder = Presentation.Group.Order.Advanced)
    protected BigDecimal qualifyingItemSubTotal;
    
    @Embedded
    protected ArchiveStatus archiveStatus = new ArchiveStatus();

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public OfferType getType() {
        return OfferType.getInstance(type);
    }

    @Override
    public void setType(OfferType type) {
        this.type = type.getType();
    }

    @Override
    public OfferDiscountType getDiscountType() {
        return OfferDiscountType.getInstance(discountType);
    }

    @Override
    public void setDiscountType(OfferDiscountType discountType) {
        this.discountType = discountType.getType();
    }
    
    @Override
    public OfferItemRestrictionRuleType getOfferItemQualifierRuleType() {
        return OfferItemRestrictionRuleType.getInstance(offerItemQualifierRuleType);
    }

    @Override
    public void setOfferItemQualifierRuleType(OfferItemRestrictionRuleType restrictionRuleType) {
        this.offerItemQualifierRuleType = restrictionRuleType.getType();
    }
    
    @Override
    public OfferItemRestrictionRuleType getOfferItemTargetRuleType() {
        return OfferItemRestrictionRuleType.getInstance(offerItemTargetRuleType);
    }

    @Override
    public void setOfferItemTargetRuleType(OfferItemRestrictionRuleType restrictionRuleType) {
        this.offerItemTargetRuleType = restrictionRuleType.getType();
    }

    @Override
    public BigDecimal getValue() {
        return value;
    }

    @Override
    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public Date getStartDate() {
        if ('Y'==getArchived()) {
            return null;
        }
        return startDate;
    }

    @Override
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns true if this offer can be stacked on top of another offer.  Stackable is evaluated
     * against offers with the same offer type.
     *
     * @return true if stackable, otherwise false
     */
    @Override
    public boolean isStackable() {
        return stackable;
    }

    /**
     * Sets the stackable value for this offer.
     *
     * @param stackable
     */
    @Override
    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }

    @Deprecated
    @JsonIgnore
    public boolean getStackable(){
        return stackable;
    }
    
    @Override
    public String getTargetSystem() {
        return targetSystem;
    }

    @Override
    public void setTargetSystem(String targetSystem) {
        this.targetSystem = targetSystem;
    }

    @Override
    public boolean getApplyDiscountToSalePrice() {
        return applyToSalePrice;
    }

    @Override
    public void setApplyDiscountToSalePrice(boolean applyToSalePrice) {
        this.applyToSalePrice=applyToSalePrice;
    }

    @Override
    @Deprecated
    public String getAppliesToOrderRules() {
        return appliesToOrderRules;
    }

    @Override
    @Deprecated
    public void setAppliesToOrderRules(String appliesToOrderRules) {
        this.appliesToOrderRules = appliesToOrderRules;
    }

    @Override
    @Deprecated
    public String getAppliesToCustomerRules() {
        return appliesToCustomerRules;
    }

    @Override
    @Deprecated
    public void setAppliesToCustomerRules(String appliesToCustomerRules) {
        this.appliesToCustomerRules = appliesToCustomerRules;
    }

    @Override
    @Deprecated
    public boolean isApplyDiscountToMarkedItems() {
        return applyDiscountToMarkedItems;
    }

    @Deprecated
    @JsonIgnore
    public boolean getApplyDiscountToMarkedItems() {
        return applyDiscountToMarkedItems;
    }
    
    @Override
    @Deprecated
    public void setApplyDiscountToMarkedItems(boolean applyDiscountToMarkedItems) {
        this.applyDiscountToMarkedItems = applyDiscountToMarkedItems;
    }

    /**
     * Returns true if this offer can be combined with other offers in the order.
     *
     * @return true if combinableWithOtherOffers, otherwise false
     */
    @Override
    public boolean isCombinableWithOtherOffers() {
        return combinableWithOtherOffers;
    }

    /**
     * Sets the combinableWithOtherOffers value for this offer.
     *
     * @param combinableWithOtherOffers
     */
    @Override
    public void setCombinableWithOtherOffers(boolean combinableWithOtherOffers) {
        this.combinableWithOtherOffers = combinableWithOtherOffers;
    }

    @Deprecated
    @JsonIgnore
    public boolean getCombinableWithOtherOffers() {
        return combinableWithOtherOffers;
    }
    
    @Override
    public OfferDeliveryType getDeliveryType() {
        return OfferDeliveryType.getInstance(deliveryType);
    }

    @Override
    public void setDeliveryType(OfferDeliveryType deliveryType) {
        this.deliveryType = deliveryType.getType();
    }

    @Override
    public Long getMaxUsesPerCustomer() {
        return maxUsesPerCustomer;
    }

    @Override
    public void setMaxUsesPerCustomer(Long maxUsesPerCustomer) {
        this.maxUsesPerCustomer = maxUsesPerCustomer;
    }

    public int getMaxUsesPerOrder() {
        return maxUsesPerOrder;
    }

    public void setMaxUsesPerOrder(int maxUsesPerOrder) {
        this.maxUsesPerOrder = maxUsesPerOrder;
    }

    @Override
    public int getMaxUses() {
        return maxUsesPerOrder;
    }

    @Override
    public void setMaxUses(int maxUses) {
        this.maxUsesPerOrder = maxUses;
    }

    @Override
    @Deprecated
    public int getUses() {
        return uses;
    }

    @Override
    public String getMarketingMessage() {
        return marketingMessage;
    }

    @Override
    public void setMarketingMessage(String marketingMessage) {
        this.marketingMessage = marketingMessage;
    }

    @Override
    @Deprecated
    public void setUses(int uses) {
        this.uses = uses;
    }

    @Override
    public Set<OfferItemCriteria> getQualifyingItemCriteria() {
        return qualifyingItemCriteria;
    }

    @Override
    public void setQualifyingItemCriteria(Set<OfferItemCriteria> qualifyingItemCriteria) {
        this.qualifyingItemCriteria = qualifyingItemCriteria;
    }

    @Override
    public Set<OfferItemCriteria> getTargetItemCriteria() {
        return targetItemCriteria;
    }

    @Override
    public void setTargetItemCriteria(Set<OfferItemCriteria> targetItemCriteria) {
        this.targetItemCriteria = targetItemCriteria;
    }

    @Override
    public Boolean isTotalitarianOffer() {
        if (totalitarianOffer == null) {
            return false;
        } else {
            return totalitarianOffer.booleanValue();
        }
    }

    @Override
    public void setTotalitarianOffer(Boolean totalitarianOffer) {
        if (totalitarianOffer == null) {
            this.totalitarianOffer = false;
        } else {
            this.totalitarianOffer = totalitarianOffer;
        }
    }

    @Override
    public Map<String, OfferRule> getOfferMatchRules() {
        if (offerMatchRules == null) {
            offerMatchRules = new HashMap<String, OfferRule>();
        }
        return offerMatchRules;
    }

    @Override
    public void setOfferMatchRules(Map<String, OfferRule> offerMatchRules) {
        this.offerMatchRules = offerMatchRules;
    }

    @Override
    public Boolean getTreatAsNewFormat() {
        return treatAsNewFormat;
    }

    @Override
    public void setTreatAsNewFormat(Boolean treatAsNewFormat) {
        this.treatAsNewFormat = treatAsNewFormat;
    }

    @Override
    public Character getArchived() {
        if (archiveStatus == null) {
            archiveStatus = new ArchiveStatus();
        }
        return archiveStatus.getArchived();
    }

    @Override
    public void setArchived(Character archived) {
        if (archiveStatus == null) {
            archiveStatus = new ArchiveStatus();
        }
        archiveStatus.setArchived(archived);
    }

    @Override
    public boolean isActive() {
        return DateUtil.isActive(startDate, endDate, true) && 'Y'!=getArchived();
    }
    
    @Override
    public Money getQualifyingItemSubTotal() {
        return qualifyingItemSubTotal == null ? null : BroadleafCurrencyUtils.getMoney(qualifyingItemSubTotal, null);
    }

    @Override
    public void setQualifyingItemSubTotal(Money qualifyingItemSubTotal) {
        this.qualifyingItemSubTotal = Money.toAmount(qualifyingItemSubTotal);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        OfferImpl other = (OfferImpl) obj;

        if (id != null && other.id != null) {
            return id.equals(other.id);
        }
        
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (startDate == null) {
            if (other.startDate != null) {
                return false;
            }
        } else if (!startDate.equals(other.startDate)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    public static class Presentation {
        public static class Tab {
            public static class Name {
                public static final String Advanced = "OfferImpl_Advanced_Tab";
            }
            
            public static class Order {
                public static final int Advanced = 1000;
            }
        }
            
        public static class Group {
            public static class Name {
                public static final String Description = "OfferImpl_Description";
                public static final String Amount = "OfferImpl_Amount";
                public static final String ActivityRange = "OfferImpl_Activity_Range";
                public static final String Qualifiers = "OfferImpl_Qualifiers";
                public static final String ItemTarget = "OfferImpl_Item_Target";
                public static final String Advanced = "OfferImpl_Advanced";
            }
            
            public static class Order {
                public static final int Description = 1000;
                public static final int Amount = 2000;
                public static final int ActivityRange = 3000;
                public static final int Qualifiers = 4000;
                public static final int ItemTarget = 5000;
                public static final int Advanced = 1000;
            }
        }
    }
}