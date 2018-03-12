package com.shopapp.shopify

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.shopapp.shopify.api.entity.ApiCountry
import com.shopapp.shopify.api.entity.ApiState
import com.shopify.buy3.Storefront
import com.shopify.graphql.support.ID
import org.joda.time.DateTime
import java.math.BigDecimal

object StorefrontMockInstantiator {
    const val DEFAULT_ID = "default_id"
    const val DEFAULT_NUMBER_ID = 0L
    const val DEFAULT_SRC = "default_src"
    const val DEFAULT_ALT_TEXT = "default_alt_text"
    const val DEFAULT_NAME = "default_name"
    const val DEFAULT_SHOP_NAME = "default_shop_name"
    const val DEFAULT_VALUE = "default_value"
    const val DEFAULT_TITLE = "default_title"
    const val DEFAULT_DESCRIPTION = "default_description"
    const val DEFAULT_BODY = "default_body"
    const val DEFAULT_QUANTITY = 5
    const val DEFAULT_ADDRESS = "default_address"
    const val DEFAULT_CITY = "default_city"
    const val DEFAULT_COUNTRY = "default_country"
    const val DEFAULT_COUNTRY_CODE = "default_country_code"
    const val DEFAULT_FIRST_NAME = "default_first_name"
    const val DEFAULT_LAST_NAME = "default_last_name"
    const val DEFAULT_BIO = "default_bio"
    const val DEFAULT_PHONE = "default_phone"
    const val DEFAULT_STATE = "default_province"
    const val DEFAULT_STATE_CODE = "default_state_code"
    const val DEFAULT_ZIP = "default_zip"
    const val DEFAULT_EMAIL = "default@email.com"
    const val DEFAULT_URL = "https://default.com"
    const val DEFAULT_HTML = "<html><head></head><body>default_content_html</body></html>"
    const val DEFAULT_VENDOR = "default_vendor"
    const val DEFAULT_TYPE = "default_type"
    const val DEFAULT_ITEM = "default_item"
    const val DEFAULT_HANDLE = "default_handle"
    const val DEFAULT_ORDER_NUMBER = 15
    const val DEFAULT_LIST_SIZE = 5
    const val DEFAULT_ACCEPT_MARKETING = false
    const val DEFAULT_REQUIRES_SHIPPING = false
    const val DEFAULT_ERROR_MESSAGE = "default_error_message"
    val DEFAULT_LIST_STRING = listOf(DEFAULT_ITEM, DEFAULT_ITEM, DEFAULT_ITEM)
    val DEFAULT_PRICE: BigDecimal = BigDecimal.TEN
    val DEFAULT_TAX: BigDecimal = BigDecimal.ONE
    val DEFAULT_DATE: DateTime = DateTime.now()
    val DEFAULT_CURRENCY_CODE: Storefront.CurrencyCode = Storefront.CurrencyCode.USD

    fun <T> newList(item: T, size: Int = DEFAULT_LIST_SIZE): List<T> {
        val list: MutableList<T> = mutableListOf()
        repeat(size) {
            list.add(item)
        }
        return list
    }

    fun newID() = spy(ID(DEFAULT_ID))

    fun newImage(): Storefront.Image = mock {
        on { id } doReturn newID()
        on { src } doReturn DEFAULT_SRC
        on { altText } doReturn DEFAULT_ALT_TEXT
    }


    fun newSelectedOption(): Storefront.SelectedOption = mock {
        on { name } doReturn DEFAULT_NAME
        on { value } doReturn DEFAULT_VALUE
    }

    fun newProductOption(): Storefront.ProductOption = mock {
        on { id } doReturn newID()
        on { name } doReturn DEFAULT_NAME
        on { values } doReturn DEFAULT_LIST_STRING
    }

    fun newProductVariant(): Storefront.ProductVariant = mock {

        val imageMock = newImage()
        val selectedOption = newSelectedOption()
        val partialProduct = newPartialProduct()

        on { id } doReturn newID()
        on { title } doReturn DEFAULT_TITLE
        on { price } doReturn DEFAULT_PRICE
        on { availableForSale } doReturn true
        on { image } doReturn imageMock
        on { selectedOptions } doReturn listOf(selectedOption)
        on { product } doReturn partialProduct
    }

    private fun newPartialProduct(): Storefront.Product = mock {
        val imageConnection = newImageConnection()

        on { id } doReturn newID()
        on { images } doReturn imageConnection
    }

    fun newOrderLineItem(): Storefront.OrderLineItem = mock {

        val productVariantMock = newProductVariant()

        on { title } doReturn DEFAULT_TITLE
        on { quantity } doReturn DEFAULT_QUANTITY
        on { variant } doReturn productVariantMock
    }

    fun newQueryRoot(): Storefront.QueryRoot = mock {
        val shopMock = newShop()
        on { shop } doReturn shopMock
    }

    fun newAddress(): Storefront.MailingAddress = mock {
        on { id } doReturn newID()
        on { address1 } doReturn DEFAULT_ADDRESS
        on { address2 } doReturn DEFAULT_ADDRESS
        on { city } doReturn DEFAULT_CITY
        on { country } doReturn DEFAULT_COUNTRY
        on { firstName } doReturn DEFAULT_FIRST_NAME
        on { lastName } doReturn DEFAULT_LAST_NAME
        on { phone } doReturn DEFAULT_PHONE
        on { province } doReturn DEFAULT_STATE
        on { zip } doReturn DEFAULT_ZIP
    }

    fun newCustomer(): Storefront.Customer = mock {
        val addressConnection = newAddressConnection()
        val address = addressConnection.edges[0].node

        on { id } doReturn newID()
        on { email } doReturn DEFAULT_EMAIL
        on { defaultAddress } doReturn address
        on { firstName } doReturn DEFAULT_FIRST_NAME
        on { lastName } doReturn DEFAULT_LAST_NAME
        on { phone } doReturn DEFAULT_PHONE
        on { acceptsMarketing } doReturn DEFAULT_ACCEPT_MARKETING
        on { addresses } doReturn addressConnection
    }

    fun newCollection(): Storefront.Collection = mock {

        val imageMock = newImage()
        val productsMock = newProductConnection()
        on { id } doReturn newID()
        on { title } doReturn DEFAULT_TITLE
        on { description } doReturn DEFAULT_DESCRIPTION
        on { descriptionHtml } doReturn DEFAULT_HTML
        on { image } doReturn imageMock
        on { products } doReturn productsMock
        on { updatedAt } doReturn DEFAULT_DATE
    }

    fun newProduct(): Storefront.Product = mock {

        val imagesMock = newImageConnection()
        val optionsMock = newList(newProductOption())
        val variantsMock = newProductVariantConnection()

        on { id } doReturn newID()
        on { title } doReturn DEFAULT_TITLE
        on { description } doReturn DEFAULT_DESCRIPTION
        on { descriptionHtml } doReturn DEFAULT_DESCRIPTION
        on { vendor } doReturn DEFAULT_VENDOR
        on { productType } doReturn DEFAULT_TYPE
        on { createdAt } doReturn DEFAULT_DATE
        on { updatedAt } doReturn DEFAULT_DATE
        on { tags } doReturn DEFAULT_LIST_STRING
        on { images } doReturn imagesMock
        on { options } doReturn optionsMock
        on { variants } doReturn variantsMock
    }

    fun newArticleEdge(): Storefront.ArticleEdge = mock {
        val articleMock = newArticle()
        on { node } doReturn articleMock
    }

    fun newArticle(): Storefront.Article = mock {

        val tagsMock: List<String> = mock()
        val blogMock = newBlog()
        val authorMock = newAuthor()
        val imageMock = newImage()

        on { id } doReturn newID()
        on { title } doReturn DEFAULT_TITLE
        on { content } doReturn DEFAULT_DESCRIPTION
        on { contentHtml } doReturn DEFAULT_HTML
        on { image } doReturn imageMock
        on { author } doReturn authorMock
        on { tags } doReturn tagsMock
        on { blog } doReturn blogMock
        on { publishedAt } doReturn DEFAULT_DATE
        on { url } doReturn DEFAULT_URL
    }

    fun newBlog(): Storefront.Blog = mock {
        on { id } doReturn newID()
        on { title } doReturn DEFAULT_TITLE
    }

    fun newAuthor(): Storefront.ArticleAuthor = mock {
        on { firstName } doReturn DEFAULT_FIRST_NAME
        on { lastName } doReturn DEFAULT_LAST_NAME
        on { name } doReturn DEFAULT_NAME
        on { email } doReturn DEFAULT_EMAIL
        on { bio } doReturn DEFAULT_BIO
    }

    fun newOrder(): Storefront.Order = mock {
        val addressMock = newAddress()
        val lineItemsMock = newOrderLineItemConnection()
        on { id } doReturn newID()
        on { currencyCode } doReturn newCurrencyCode()
        on { email } doReturn DEFAULT_EMAIL
        on { orderNumber } doReturn DEFAULT_ORDER_NUMBER
        on { totalPrice } doReturn DEFAULT_PRICE
        on { subtotalPrice } doReturn DEFAULT_PRICE
        on { totalShippingPrice } doReturn DEFAULT_PRICE
        on { shippingAddress } doReturn addressMock
        on { processedAt } doReturn DEFAULT_DATE
        on { lineItems } doReturn lineItemsMock
    }

    fun newShop(): Storefront.Shop = mock {
        val policy = newPolicy()
        val collectionsConnectionMock = newCollectionConnection()
        val paymentSettingsMock = newPaymentSettings()

        on { name } doReturn DEFAULT_SHOP_NAME
        on { privacyPolicy } doReturn policy
        on { description } doReturn DEFAULT_DESCRIPTION
        on { refundPolicy } doReturn policy
        on { termsOfService } doReturn policy
        on { collections } doReturn collectionsConnectionMock
        on { paymentSettings } doReturn paymentSettingsMock
    }

    fun newPaymentSettings(): Storefront.PaymentSettings = mock {
        on { currencyCode } doReturn DEFAULT_CURRENCY_CODE
    }

    fun newPolicy(): Storefront.ShopPolicy = mock {
        on { title } doReturn DEFAULT_TITLE
        on { body } doReturn DEFAULT_BODY
        on { url } doReturn DEFAULT_URL
    }

    fun newOrderConnection(): Storefront.OrderConnection = mock {
        val edgeMockList = listOf(newOrderEdge())
        on { edges } doReturn edgeMockList
    }

    fun newCheckout(): Storefront.Checkout = mock {
        val addressMock = newAddress()
        val shippingLineMock = newShippingRate()

        on { id } doReturn newID()
        on { webUrl } doReturn DEFAULT_URL
        on { requiresShipping } doReturn DEFAULT_REQUIRES_SHIPPING
        on { subtotalPrice } doReturn DEFAULT_PRICE
        on { totalPrice } doReturn DEFAULT_PRICE
        on { totalTax } doReturn DEFAULT_TAX
        on { currencyCode } doReturn DEFAULT_CURRENCY_CODE
        on { shippingAddress } doReturn addressMock
        on { shippingLine } doReturn shippingLineMock
    }

    fun newShippingRate(): Storefront.ShippingRate = mock {
        on { title } doReturn DEFAULT_TITLE
        on { price } doReturn DEFAULT_PRICE
        on { handle } doReturn DEFAULT_HANDLE
    }

    fun newCountry(): ApiCountry = mock {
        val statesMock = newList(newState())

        on { id } doReturn DEFAULT_NUMBER_ID
        on { code } doReturn DEFAULT_COUNTRY_CODE
        on { name } doReturn DEFAULT_COUNTRY
        on { states } doReturn statesMock
    }

    fun newState(): ApiState = mock {
        on { id } doReturn DEFAULT_NUMBER_ID
        on { countryId } doReturn DEFAULT_NUMBER_ID
        on { code } doReturn DEFAULT_STATE_CODE
        on { name } doReturn DEFAULT_STATE
    }

    fun newUserError(): Storefront.UserError = mock {
        on { message } doReturn DEFAULT_ERROR_MESSAGE
    }

    private fun newOrderEdge(): Storefront.OrderEdge = mock {
        val orderMock = newOrder()
        on { node } doReturn orderMock
    }

    private fun newOrderLineItemConnection(): Storefront.OrderLineItemConnection = mock {
        val edgeMock = newOrderLineItemEdge()
        on { edges } doReturn listOf(edgeMock)
    }

    fun newOrderLineItemEdge(): Storefront.OrderLineItemEdge = mock {
        val orderLineItemMock = newOrderLineItem()
        on { node } doReturn orderLineItemMock
    }

    private fun newCurrencyCode() = DEFAULT_CURRENCY_CODE

    private fun newImageConnection(): Storefront.ImageConnection = mock {
        val imageEdges = newList(newImageEdge())

        on { edges } doReturn imageEdges
    }

    private fun newImageEdge(): Storefront.ImageEdge = mock {
        val image = newImage()

        on { node } doReturn image
    }

    fun newProductVariantEdge(): Storefront.ProductVariantEdge = mock {
        val variant = newProductVariant()

        on { node } doReturn variant
    }

    fun newProductVariantConnection(): Storefront.ProductVariantConnection = mock {
        val result = newList(newProductVariantEdge())

        on { edges } doReturn listOf(result)
    }

    fun newAddressConnection(): Storefront.MailingAddressConnection = mock {
        val edgeMockList = newList(newAddressEdge())
        on { edges } doReturn edgeMockList
    }

    private fun newAddressEdge(): Storefront.MailingAddressEdge = mock {
        val addressMock = newAddress()
        on { node } doReturn addressMock
    }

    private fun newCollectionConnection(): Storefront.CollectionConnection = mock {
        val collections = newList(newCollectionEdge())
        on { edges } doReturn collections
    }

    private fun newCollectionEdge(): Storefront.CollectionEdge = mock {
        val collection = newCollection()
        on { node } doReturn collection
    }

    fun newProductConnection(): Storefront.ProductConnection = mock {
        val products = newList(newProductEdge())
        on { edges } doReturn products
    }

    private fun newProductEdge(): Storefront.ProductEdge = mock {
        val product = newProduct()
        on { node } doReturn product
    }
}
