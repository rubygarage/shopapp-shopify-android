package com.shopapp.shopify.api

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.shopapp.gateway.Api
import com.shopapp.gateway.ApiCallback
import com.shopapp.gateway.entity.*
import com.shopapp.shopify.api.QueryHelper.getArticleSortKey
import com.shopapp.shopify.api.QueryHelper.getDefaultAddressQuery
import com.shopapp.shopify.api.QueryHelper.getDefaultArticleQuery
import com.shopapp.shopify.api.QueryHelper.getDefaultCheckoutQuery
import com.shopapp.shopify.api.QueryHelper.getDefaultCustomerQuery
import com.shopapp.shopify.api.QueryHelper.getDefaultCustomerUpdateMutationQuery
import com.shopapp.shopify.api.QueryHelper.getDefaultOrderQuery
import com.shopapp.shopify.api.QueryHelper.getDefaultProductQuery
import com.shopapp.shopify.api.QueryHelper.getDefaultProductVariantQuery
import com.shopapp.shopify.api.QueryHelper.getDefaultUserErrors
import com.shopapp.shopify.api.QueryHelper.getProductCollectionSortKey
import com.shopapp.shopify.api.QueryHelper.getProductSortKey
import com.shopapp.shopify.api.adapter.*
import com.shopapp.shopify.api.call.AdapterResult
import com.shopapp.shopify.api.call.MutationCallWrapper
import com.shopapp.shopify.api.call.QueryCallWrapper
import com.shopapp.shopify.api.entity.AccessData
import com.shopapp.shopify.api.entity.ApiCountry
import com.shopapp.shopify.api.entity.ApiCountryResponse
import com.shopapp.shopify.api.retrofit.CountriesService
import com.shopapp.shopify.api.retrofit.RestClient
import com.shopapp.shopify.constant.Constant.ACCESS_TOKEN
import com.shopapp.shopify.constant.Constant.AND_LOGICAL_KEY
import com.shopapp.shopify.constant.Constant.COUNTRIES_FILE_NAME
import com.shopapp.shopify.constant.Constant.DEFAULT_SCHEME
import com.shopapp.shopify.constant.Constant.EMAIL
import com.shopapp.shopify.constant.Constant.EXPIRES_DATE
import com.shopapp.shopify.constant.Constant.ITEMS_COUNT
import com.shopapp.shopify.constant.Constant.PRODUCT_TYPE_FILTER_KEY
import com.shopapp.shopify.constant.Constant.REST_OF_WORLD
import com.shopapp.shopify.constant.Constant.RETRY_HANDLER_DELAY
import com.shopapp.shopify.constant.Constant.RETRY_HANDLER_MAX_COUNT
import com.shopapp.shopify.constant.Constant.TITLE_FILTER_KEY
import com.shopapp.shopify.constant.Constant.UNAUTHORIZED_ERROR
import com.shopapp.shopify.util.AssetsReader
import com.shopify.buy3.*
import com.shopify.graphql.support.ID
import net.danlew.android.joda.JodaTimeAndroid
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


class ShopifyApi : Api {

    private val context: Context
    private val preferences: SharedPreferences
    private val graphClient: GraphClient
    private val cardClient: CardClient
    private val retrofit: Retrofit
    private val baseUrl: String
    private val assetsReader: AssetsReader

    constructor(
        context: Context,
        baseDomain: String,
        storefrontAccessToken: String,
        apiKey: String,
        apiPassword: String,
        scheme: String = DEFAULT_SCHEME
    ) {
        this.context = context
        this.baseUrl = scheme + baseDomain
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        graphClient = GraphClient.builder(context)
            .shopDomain(baseDomain)
            .accessToken(storefrontAccessToken)
            .build()
        retrofit = RestClient.providesRetrofit(baseUrl, apiKey, apiPassword)
        cardClient = CardClient()
        assetsReader = AssetsReader()

        JodaTimeAndroid.init(context)
    }

    internal constructor(
        context: Context,
        graphClient: GraphClient,
        retrofit: Retrofit,
        cardClient: CardClient,
        sharedPreferences: SharedPreferences,
        assetsReader: AssetsReader,
        baseUrl: String
    ) {
        this.context = context
        this.graphClient = graphClient
        this.retrofit = retrofit
        this.cardClient = cardClient
        this.preferences = sharedPreferences
        this.assetsReader = assetsReader
        this.baseUrl = baseUrl
    }

    /* AUTH */

    override fun signUp(firstName: String, lastName: String, email: String,
                        password: String, phone: String, callback: ApiCallback<Unit>) {

        val customerCreateInput = Storefront.CustomerCreateInput(email, password)
            .setFirstName(firstName)
            .setLastName(lastName)

        if (phone.isNotBlank()) {
            customerCreateInput.phone = phone
        }

        val customerQuery = Storefront.CustomerCreatePayloadQueryDefinition {
            it.customer { getDefaultCustomerQuery(it) }
                .userErrors { getDefaultUserErrors(it) }
        }

        val mutationQuery = Storefront.mutation { query -> query.customerCreate(customerCreateInput, customerQuery) }
        val call = graphClient.mutateGraph(mutationQuery)
        call.enqueue(object : GraphCall.Callback<Storefront.Mutation> {
            override fun onResponse(response: GraphResponse<Storefront.Mutation>) {
                val error = ErrorAdapter.adaptErrors(response.errors())
                if (error != null) {
                    callback.onFailure(error)
                } else {
                    response.data()?.customerCreate?.let { customerCreate ->
                        val userError = ErrorAdapter.adaptUserError(customerCreate.userErrors)
                        if (userError != null) {
                            callback.onFailure(userError)
                        } else if (customerCreate.customer != null) {
                            val tokenResponse = requestToken(email, password)
                            if (tokenResponse != null) {
                                tokenResponse.first?.let {
                                    callback.onResult(Unit)
                                }
                                tokenResponse.second?.let {
                                    callback.onFailure(it)
                                }
                            }
                        }
                    }
                }
            }

            override fun onFailure(error: GraphError) {
                callback.onFailure(ErrorAdapter.adapt(error))
            }
        })
    }

    override fun signIn(email: String, password: String, callback: ApiCallback<Unit>) {
        val tokenResponse = requestToken(email, password)
        if (tokenResponse != null) {
            tokenResponse.first?.let {
                callback.onResult(Unit)
            }
            tokenResponse.second?.let {
                callback.onFailure(it)
            }
        } else {
            callback.onFailure(Error.Content())
        }
    }

    override fun signOut(callback: ApiCallback<Unit>) {
        removeSession()
        callback.onResult(Unit)
    }

    override fun isLoggedIn(callback: ApiCallback<Boolean>) {
        callback.onResult(getSession() != null)
    }

    override fun forgotPassword(email: String, callback: ApiCallback<Unit>) {

        val mutationQuery = Storefront.mutation {
            it.customerRecover(email, {
                it.userErrors { getDefaultUserErrors(it) }
            })
        }

        graphClient.mutateGraph(mutationQuery).enqueue(object : GraphCall.Callback<Storefront.Mutation> {
            override fun onFailure(error: GraphError) {
                callback.onFailure(ErrorAdapter.adapt(error))
            }

            override fun onResponse(response: GraphResponse<Storefront.Mutation>) {
                val error = ErrorAdapter.adaptErrors(response.errors())
                val userError = ErrorAdapter.adaptUserError(response.data()?.customerRecover?.userErrors)
                when {
                    error != null -> callback.onFailure(error)
                    userError != null -> callback.onFailure(userError)
                    else -> callback.onResult(Unit)
                }
            }
        })
    }

    override fun getCustomer(callback: ApiCallback<Customer?>) {

        val accessData = getSession()

        if (accessData != null) {
            val query = Storefront.query {
                it.customer(accessData.accessToken) { getDefaultCustomerQuery(it) }
            }
            graphClient.queryGraph(query).enqueue(object : QueryCallWrapper<Customer?>(callback) {
                override fun adapt(data: Storefront.QueryRoot): AdapterResult<Customer?> {
                    val adaptee = data.customer
                    return if (adaptee != null) {
                        AdapterResult.DataResult(CustomerAdapter.adapt(adaptee))
                    } else {
                        removeSession()
                        AdapterResult.ErrorResult(Error.Content())
                    }
                }
            })
        } else {
            callback.onFailure(Error.NonCritical(UNAUTHORIZED_ERROR))
        }
    }

    override fun createCustomerAddress(address: Address, callback: ApiCallback<String>) {

        val session = getSession()
        if (session == null) {
            callback.onFailure(Error.NonCritical(UNAUTHORIZED_ERROR))
        } else {
            val mailingAddressInput = Storefront.MailingAddressInput()
                .setAddress1(address.address)
                .setAddress2(address.secondAddress)
                .setCity(address.city)
                .setProvince(address.state)
                .setCountry(address.country)
                .setFirstName(address.firstName)
                .setLastName(address.lastName)
                .setPhone(address.phone)
                .setZip(address.zip)

            val mutation = Storefront.mutation {
                it.customerAddressCreate(session.accessToken, mailingAddressInput, {
                    it.customerAddress { it.firstName() }
                    it.userErrors { getDefaultUserErrors(it) }
                })
            }

            graphClient.mutateGraph(mutation).enqueue(object : MutationCallWrapper<String>(callback) {
                override fun adapt(data: Storefront.Mutation?): AdapterResult<String>? {
                    return data?.customerAddressCreate?.let {
                        val userError = ErrorAdapter.adaptUserError(it.userErrors)
                        return if (userError != null) {
                            AdapterResult.ErrorResult(userError)
                        } else {
                            AdapterResult.DataResult(it.customerAddress.id.toString())
                        }
                    }
                }
            })
        }
    }

    override fun editCustomerAddress(addressId: String, address: Address, callback: ApiCallback<Unit>) {

        val session = getSession()
        if (session == null) {
            callback.onFailure(Error.NonCritical(UNAUTHORIZED_ERROR))
        } else {
            val mutation = Storefront.mutation {

                val mailingAddressInput = Storefront.MailingAddressInput()
                    .setAddress1(address.address)
                    .setAddress2(address.secondAddress)
                    .setCity(address.city)
                    .setProvince(address.state)
                    .setCountry(address.country)
                    .setFirstName(address.firstName)
                    .setLastName(address.lastName)
                    .setPhone(address.phone)
                    .setZip(address.zip)

                it.customerAddressUpdate(session.accessToken, ID(addressId), mailingAddressInput, {
                    it.customerAddress { it.firstName() }
                    it.userErrors { getDefaultUserErrors(it) }
                })
            }

            graphClient.mutateGraph(mutation).enqueue(object : MutationCallWrapper<Unit>(callback) {
                override fun adapt(data: Storefront.Mutation?): AdapterResult<Unit>? {
                    return data?.customerAddressUpdate?.let {
                        val userError = ErrorAdapter.adaptUserError(it.userErrors)
                        return if (userError != null) {
                            AdapterResult.ErrorResult(userError)
                        } else {
                            AdapterResult.DataResult(Unit)
                        }
                    }
                }
            })
        }
    }

    override fun deleteCustomerAddress(addressId: String, callback: ApiCallback<Unit>) {

        val session = getSession()
        if (session == null) {
            callback.onFailure(Error.NonCritical(UNAUTHORIZED_ERROR))
        } else {
            val mutation = Storefront.mutation {
                it.customerAddressDelete(ID(addressId), session.accessToken, {
                    it.deletedCustomerAddressId()
                    it.userErrors { getDefaultUserErrors(it) }
                })
            }

            graphClient.mutateGraph(mutation).enqueue(object : MutationCallWrapper<Unit>(callback) {
                override fun adapt(data: Storefront.Mutation?): AdapterResult<Unit>? {
                    return data?.customerAddressDelete?.let {
                        val userError = ErrorAdapter.adaptUserError(it.userErrors)
                        return if (userError != null) {
                            AdapterResult.ErrorResult(userError)
                        } else {
                            AdapterResult.DataResult(Unit)
                        }
                    }
                }
            })
        }
    }

    override fun setDefaultShippingAddress(addressId: String, callback: ApiCallback<Unit>) {
        val session = getSession()
        if (session == null) {
            callback.onFailure(Error.NonCritical(UNAUTHORIZED_ERROR))
        } else {
            val mutation = Storefront.mutation {
                it.customerDefaultAddressUpdate(session.accessToken, ID(addressId), {
                    it.customer { it.firstName() }
                    it.userErrors { getDefaultUserErrors(it) }
                })
            }

            graphClient.mutateGraph(mutation).enqueue(object : MutationCallWrapper<Unit>(callback) {
                override fun adapt(data: Storefront.Mutation?): AdapterResult<Unit>? {
                    return data?.customerDefaultAddressUpdate?.let {
                        val userError = ErrorAdapter.adaptUserError(it.userErrors)
                        return if (userError != null) {
                            AdapterResult.ErrorResult(userError)
                        } else {
                            AdapterResult.DataResult(Unit)
                        }
                    }
                }
            })
        }
    }

    override fun editCustomerInfo(firstName: String, lastName: String, phone: String, callback: ApiCallback<Customer>) {
        val session = getSession()
        if (session == null) {
            callback.onFailure(Error.NonCritical(UNAUTHORIZED_ERROR))
        } else {

            val customerInput = Storefront.CustomerUpdateInput()
                .setFirstName(firstName)
                .setLastName(lastName)

            if (phone.isNotBlank()) {
                customerInput.phone = phone
            }

            val mutateQuery = getDefaultCustomerUpdateMutationQuery(session.accessToken, customerInput)

            graphClient.mutateGraph(mutateQuery).enqueue(object : MutationCallWrapper<Customer>(callback) {
                override fun adapt(data: Storefront.Mutation?): AdapterResult<Customer>? {
                    return data?.customerUpdate?.let {
                        val userError = ErrorAdapter.adaptUserError(it.userErrors)
                        return if (userError != null) {
                            AdapterResult.ErrorResult(userError)
                        } else {
                            AdapterResult.DataResult(CustomerAdapter.adapt(it.customer))
                        }
                    }
                }
            })
        }
    }

    override fun changePassword(password: String, callback: ApiCallback<Unit>) {
        val session = getSession()
        if (session == null) {
            callback.onFailure(Error.NonCritical(UNAUTHORIZED_ERROR))
        } else {

            val customerInput = Storefront.CustomerUpdateInput()
                .setPassword(password)

            val mutateQuery = getDefaultCustomerUpdateMutationQuery(session.accessToken, customerInput)

            graphClient.mutateGraph(mutateQuery).enqueue(object : MutationCallWrapper<Unit>(callback) {
                override fun adapt(data: Storefront.Mutation?): AdapterResult<Unit>? {
                    return data?.customerUpdate?.let {
                        val userError = ErrorAdapter.adaptUserError(it.userErrors)
                        if (userError != null) {
                            return AdapterResult.ErrorResult(userError)
                        } else {
                            val tokenResponse = requestToken(it.customer.email, password)
                            if (tokenResponse != null) {
                                tokenResponse.first?.let {
                                    return AdapterResult.DataResult(Unit)
                                }
                                tokenResponse.second?.let {
                                    return AdapterResult.ErrorResult(it)
                                }
                            }
                        }
                        return null
                    }
                }
            })
        }
    }

    override fun getCountries(callback: ApiCallback<List<Country>>) {
        val countryService = retrofit.create(CountriesService::class.java)

        countryService.getCountries().enqueue(object : Callback<ApiCountryResponse> {
            override fun onResponse(call: Call<ApiCountryResponse>?, response: Response<ApiCountryResponse>?) {
                if (response != null) {
                    if (response.isSuccessful && response.body() != null) {
                        val countries = CountryListAdapter.adapt(response.body()?.countries)
                        if (countries.any { it.name == REST_OF_WORLD }) {
                            callback.onResult(CountryListAdapter.adapt(getAllCountriesList()))
                        } else {
                            callback.onResult(countries)
                        }
                    } else if (response.errorBody() != null) {
                        callback.onFailure(Error.Content())
                    }
                }
            }

            override fun onFailure(call: Call<ApiCountryResponse>?, t: Throwable?) {
                callback.onFailure(Error.Content())
            }
        })
    }

    override fun updateCustomerSettings(isAcceptMarketing: Boolean, callback: ApiCallback<Unit>) {
        val session = getSession()
        if (session == null) {
            callback.onFailure(Error.NonCritical(UNAUTHORIZED_ERROR))
        } else {

            val customerInput = Storefront.CustomerUpdateInput()
                .setAcceptsMarketing(isAcceptMarketing)

            val mutateQuery = getDefaultCustomerUpdateMutationQuery(session.accessToken, customerInput)

            graphClient.mutateGraph(mutateQuery).enqueue(object : MutationCallWrapper<Unit>(callback) {
                override fun adapt(data: Storefront.Mutation?): AdapterResult<Unit>? {
                    return data?.customerUpdate?.let {
                        val userError = ErrorAdapter.adaptUserError(it.userErrors)
                        return if (userError != null) {
                            AdapterResult.ErrorResult(userError)
                        } else {
                            AdapterResult.DataResult(Unit)
                        }
                    }
                }
            })

        }
    }

    /* PRODUCT */

    override fun getProduct(id: String, callback: ApiCallback<Product>) {

        val nodeId = ID(id)
        val query = Storefront.query { queryBuilder ->
            queryBuilder
                .shop { shopQueryBuilder -> shopQueryBuilder.paymentSettings({ it.currencyCode() }) }
                .node(nodeId) { nodeQuery ->
                    nodeQuery.id()
                    nodeQuery.onProduct { productQuery ->
                        getDefaultProductQuery(productQuery)
                            .descriptionHtml()
                            .images({ it.first(ITEMS_COUNT) }, { imageConnectionQuery ->
                                imageConnectionQuery.edges({ imageEdgeQuery ->
                                    imageEdgeQuery.node({ QueryHelper.getDefaultImageQuery(it) })
                                })
                            })
                            .variants({ it.first(ITEMS_COUNT) }) { productVariantConnectionQuery ->
                                productVariantConnectionQuery
                                    .edges { productVariantEdgeQuery ->
                                        productVariantEdgeQuery
                                            .node { productVariantQuery ->
                                                getDefaultProductVariantQuery(productVariantQuery)
                                            }
                                    }
                            }

                    }
                }
        }

        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<Product>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<Product> {
                val productAdaptee = data.node as? Storefront.Product
                return if (productAdaptee != null) {
                    AdapterResult.DataResult(ProductAdapter.adapt(data.shop, productAdaptee))
                } else {
                    AdapterResult.ErrorResult(Error.Critical())
                }
            }
        })
    }

    override fun getProductVariantList(productVariantIdList: List<String>, callback: ApiCallback<List<ProductVariant>>) {

        val query = Storefront.query {
            val ids = productVariantIdList.map { ID(it) }
            it.shop { shopQueryBuilder -> shopQueryBuilder.paymentSettings({ it.currencyCode() }) }
            it.nodes(ids) {
                it.id()
                it.onProductVariant { getDefaultProductVariantQuery(it) }
            }
        }

        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<List<ProductVariant>>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<List<ProductVariant>> {
                val result: List<ProductVariant> = data.nodes
                    .mapNotNull { it as? Storefront.ProductVariant }
                    .map { ProductVariantAdapter.adapt(it) }
                return AdapterResult.DataResult(result)
            }
        })
    }

    override fun getProductList(perPage: Int, paginationValue: Any?, sortBy: SortType?,
                                keyword: String?, excludeKeyword: String?,
                                callback: ApiCallback<List<Product>>) {
        val reverse = sortBy == SortType.RECENT
        var phrase = keyword
        if (sortBy == SortType.TYPE && keyword != null) {
            phrase = "-$TITLE_FILTER_KEY\"$excludeKeyword\" $AND_LOGICAL_KEY $PRODUCT_TYPE_FILTER_KEY\"$keyword\""
        }
        queryProducts(perPage, paginationValue, phrase, reverse, sortBy, callback)
    }

    override fun searchProductList(perPage: Int, paginationValue: Any?,
                                   searchQuery: String, callback: ApiCallback<List<Product>>) {
        queryProducts(perPage, paginationValue, searchQuery, false, SortType.NAME, callback)
    }

    /* CATEGORY */

    override fun getCategoryDetails(id: String, perPage: Int, paginationValue: Any?, sortBy: SortType?,
                                    callback: ApiCallback<Category>) {

        val reverse = sortBy == SortType.RECENT || sortBy == SortType.PRICE_HIGH_TO_LOW

        val nodeId = ID(id)
        val query = Storefront.query { rootQuery ->
            rootQuery
                .shop { shopQuery -> shopQuery.paymentSettings({ it.currencyCode() }) }
                .node(nodeId) { nodeQuery ->
                    nodeQuery
                        .id()
                        .onCollection { builder ->
                            builder.title()
                                .description()
                                .descriptionHtml()
                                .updatedAt()
                                .image({ QueryHelper.getDefaultImageQuery(it) })
                                .products({ args ->
                                    args.first(ITEMS_COUNT)
                                    if (paginationValue != null) {
                                        args.after(paginationValue.toString())
                                    }
                                    val key = getProductCollectionSortKey(sortBy)
                                    if (key != null) {
                                        args.sortKey(key)
                                    }
                                    args.reverse(reverse)
                                }, { productConnectionQuery ->
                                    productConnectionQuery.edges({ productEdgeQuery ->
                                        productEdgeQuery
                                            .cursor()
                                            .node({ productQuery ->
                                                getDefaultProductQuery(productQuery)
                                                    .images({ it.first(1) }, { imageConnectionQuery ->
                                                        imageConnectionQuery.edges({ imageEdgeQuery ->
                                                            imageEdgeQuery.node({ QueryHelper.getDefaultImageQuery(it) })
                                                        })
                                                    })
                                                    .variants({ it.first(ITEMS_COUNT) }) { productVariantConnectionQuery ->
                                                        productVariantConnectionQuery.edges { productVariantEdgeQuery ->
                                                            productVariantEdgeQuery.node({ it.price() })
                                                        }
                                                    }
                                            })
                                    })
                                })
                        }
                }
        }

        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<Category>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<Category> {
                val collectionAdaptee = data.node as? Storefront.Collection
                return if (collectionAdaptee != null) {
                    AdapterResult.DataResult(CategoryAdapter.adapt(data.shop, collectionAdaptee))
                } else {
                    AdapterResult.ErrorResult(Error.Critical())
                }
            }
        })
    }

    override fun getCategoryList(perPage: Int, paginationValue: Any?, callback: ApiCallback<List<Category>>) {

        val query = Storefront.query { rootQuery ->
            rootQuery.shop { shopQuery ->
                shopQuery.collections({ args ->
                    args.first(ITEMS_COUNT)
                    if (paginationValue != null) {
                        args.after(paginationValue.toString())
                    }
                }) {
                    it.edges {
                        it.cursor()
                            .node {
                                it.title()
                                    .description()
                                    .updatedAt()
                                    .image({ QueryHelper.getDefaultImageQuery(it) })
                            }
                    }
                }
            }
        }

        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<List<Category>>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<List<Category>> {
                return AdapterResult.DataResult(CategoryListAdapter.adapt(data.shop))
            }
        })
    }

    /* BLOG */

    override fun getArticle(id: String, callback: ApiCallback<Pair<Article, String>>) {
        val nodeId = ID(id)
        val query = Storefront.query {
            it.node(nodeId) {
                it.onArticle {
                    getDefaultArticleQuery(it)
                }
            }
        }

        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<Pair<Article, String>>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<Pair<Article, String>> {
                val articleAdaptee = data.node as? Storefront.Article
                return if (articleAdaptee != null) {
                    AdapterResult.DataResult(Pair(ArticleAdapter.adapt(articleAdaptee), baseUrl))
                } else {
                    AdapterResult.ErrorResult(Error.Critical())
                }
            }
        })
    }

    override fun getArticleList(perPage: Int, paginationValue: Any?, sortBy: SortType?,
                                reverse: Boolean, callback: ApiCallback<List<Article>>) {
        val query = Storefront.query { rootQuery ->
            rootQuery.shop { shopQuery ->
                shopQuery.articles({ args ->
                    args.first(perPage)
                    if (paginationValue != null) {
                        args.after(paginationValue.toString())
                    }
                    val key = getArticleSortKey(sortBy)
                    if (key != null) {
                        args.sortKey(key)
                    }
                    args.reverse(reverse)
                }) { articleConnectionQuery ->
                    articleConnectionQuery.edges { articleEdgeQuery ->
                        articleEdgeQuery.cursor().node {
                            getDefaultArticleQuery(it)
                        }
                    }
                }
            }
        }

        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<List<Article>>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<List<Article>> {
                return AdapterResult.DataResult(ArticleListAdapter.adapt(data.shop.articles.edges))
            }
        })
    }

    /* SHOP */

    override fun getShopInfo(callback: ApiCallback<Shop>) {

        val query = Storefront.query { rootQuery ->
            rootQuery.shop { shopQuery ->
                shopQuery
                    .name()
                    .description()
                    .privacyPolicy { shopPolicyQuery -> shopPolicyQuery.title().body().url() }
                    .refundPolicy { refundPolicyQuery -> refundPolicyQuery.title().body().url() }
                    .termsOfService { termsOfServiceQuery -> termsOfServiceQuery.title().body().url() }
            }
        }

        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<Shop>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<Shop> {
                return AdapterResult.DataResult(ShopAdapter.adapt(data))
            }
        })
    }

    /* ORDER */

    override fun getOrders(perPage: Int, paginationValue: Any?, callback: ApiCallback<List<Order>>) {
        val session = getSession()
        if (session == null) {
            callback.onFailure(Error.NonCritical(UNAUTHORIZED_ERROR))
        } else {
            val query = Storefront.query { root ->
                root.customer(session.accessToken) { customer ->
                    customer.orders({ args ->
                        args.first(perPage)
                        if (paginationValue != null) {
                            args.after(paginationValue.toString())
                        }
                        args.reverse(true)
                    }
                    ) { connection ->
                        connection.edges { edge ->
                            edge.cursor().node { node ->
                                getDefaultOrderQuery(node)
                            }
                        }
                    }
                }
            }

            val call = graphClient.queryGraph(query)
            call.enqueue(object : QueryCallWrapper<List<Order>>(callback) {
                override fun adapt(data: Storefront.QueryRoot): AdapterResult<List<Order>> =
                    AdapterResult.DataResult(OrderListAdapter.adapt(data.customer.orders))
            })

        }
    }

    override fun getOrder(orderId: String, callback: ApiCallback<Order>) {

        val nodeId = ID(orderId)
        val query = Storefront.query { root ->
            root.node(nodeId) {
                it.onOrder {
                    getDefaultOrderQuery(it)
                        .subtotalPrice()
                        .totalShippingPrice()
                        .shippingAddress {
                            getDefaultAddressQuery(it)
                        }
                }
            }
        }

        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<Order>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<Order> {
                val orderAdaptee = data.node as? Storefront.Order
                return if (orderAdaptee != null) {
                    AdapterResult.DataResult(OrderAdapter.adapt(orderAdaptee, isRemoveSingleOptions = true))
                } else {
                    AdapterResult.ErrorResult(Error.Critical())
                }
            }
        })

    }

    /* CHECKOUT */

    override fun createCheckout(cartProductList: List<CartProduct>, callback: ApiCallback<Checkout>) {

        val input = Storefront.CheckoutCreateInput().setLineItems(
            cartProductList.map {
                Storefront.CheckoutLineItemInput(it.quantity, ID(it.productVariant.id))
            }
        )

        val mutateQuery = Storefront.mutation {
            it.checkoutCreate(input) {
                it.checkout { getDefaultCheckoutQuery(it) }
                    .userErrors { getDefaultUserErrors(it) }
            }
        }

        graphClient.mutateGraph(mutateQuery).enqueue(object : MutationCallWrapper<Checkout>(callback) {
            override fun adapt(data: Storefront.Mutation?): AdapterResult<Checkout>? {
                return data?.checkoutCreate?.let {
                    val userError = ErrorAdapter.adaptUserError(it.userErrors)
                    return if (userError != null) {
                        AdapterResult.ErrorResult(userError)
                    } else {
                        AdapterResult.DataResult(CheckoutAdapter.adapt(it.checkout))
                    }
                }
            }
        })
    }

    override fun getCheckout(checkoutId: String, callback: ApiCallback<Checkout>) {
        val query = Storefront.query({
            it.node(ID(checkoutId), {
                it.onCheckout {
                    getDefaultCheckoutQuery(it)
                }
            })
        })

        graphClient.queryGraph(query).enqueue(object : QueryCallWrapper<Checkout>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<Checkout> {
                val checkoutAdaptee = data.node as? Storefront.Checkout
                return if (checkoutAdaptee != null) {
                    AdapterResult.DataResult(CheckoutAdapter.adapt(checkoutAdaptee))
                } else {
                    AdapterResult.ErrorResult(Error.Critical())
                }
            }
        })
    }

    override fun setShippingAddress(checkoutId: String, address: Address, callback: ApiCallback<Checkout>) {

        val mailingAddressInput = Storefront.MailingAddressInput()
            .setAddress1(address.address)
            .setAddress2(address.secondAddress)
            .setCity(address.city)
            .setProvince(address.state)
            .setCountry(address.country)
            .setFirstName(address.firstName)
            .setLastName(address.lastName)
            .setPhone(address.phone)
            .setZip(address.zip)

        val checkoutQuery = Storefront.mutation {
            it.checkoutShippingAddressUpdate(mailingAddressInput, ID(checkoutId), {
                it.checkout { getDefaultCheckoutQuery(it) }
                it.userErrors { getDefaultUserErrors(it) }
            })
        }

        graphClient.mutateGraph(checkoutQuery).enqueue(object : MutationCallWrapper<Checkout>(callback) {
            override fun adapt(data: Storefront.Mutation?): AdapterResult<Checkout>? {
                return data?.checkoutShippingAddressUpdate?.let {
                    val userError = ErrorAdapter.adaptUserError(it.userErrors)
                    return if (userError != null) {
                        AdapterResult.ErrorResult(userError)
                    } else {
                        AdapterResult.DataResult(CheckoutAdapter.adapt(data.checkoutShippingAddressUpdate.checkout))
                    }
                }
            }
        })
    }

    override fun getShippingRates(checkoutId: String, callback: ApiCallback<List<ShippingRate>>) {

        val retryHandler = RetryHandler.delay(RETRY_HANDLER_DELAY, TimeUnit.MILLISECONDS)
            .maxCount(RETRY_HANDLER_MAX_COUNT)
            .whenResponse<Storefront.QueryRoot> {
                val checkout = (it.data()?.node as? Storefront.Checkout)
                checkout?.availableShippingRates?.let { !it.ready } ?: true
            }
            .build()

        val query = Storefront.query {
            it.node(ID(checkoutId), {
                it.onCheckout {
                    it.availableShippingRates {
                        it.ready()
                            .shippingRates {
                                it.title()
                                    .price()
                                    .handle()
                            }
                    }
                }
            })
        }

        graphClient.queryGraph(query).enqueue(object : GraphCall.Callback<Storefront.QueryRoot> {

            override fun onResponse(response: GraphResponse<Storefront.QueryRoot>) {
                val checkout = (response.data()?.node as? Storefront.Checkout)
                callback.onResult(checkout?.availableShippingRates?.shippingRates?.let {
                    it.map { ShippingRateAdapter.adapt(it) }
                } ?: emptyList())
            }

            override fun onFailure(error: GraphError) {
                callback.onFailure(ErrorAdapter.adapt(error))
            }
        }, null, retryHandler)
    }

    override fun selectShippingRate(checkoutId: String, shippingRate: ShippingRate, callback: ApiCallback<Checkout>) {

        val checkoutQuery = Storefront.mutation {
            it.checkoutShippingLineUpdate(ID(checkoutId), shippingRate.handle, {
                it.userErrors { getDefaultUserErrors(it) }
                    .checkout {
                        getDefaultCheckoutQuery(it)
                    }
            })
        }

        graphClient.mutateGraph(checkoutQuery).enqueue(object : MutationCallWrapper<Checkout>(callback) {
            override fun adapt(data: Storefront.Mutation?): AdapterResult<Checkout>? {
                return data?.checkoutShippingLineUpdate?.let {
                    val userError = ErrorAdapter.adaptUserError(it.userErrors)
                    return if (userError != null) {
                        AdapterResult.ErrorResult(userError)
                    } else {
                        AdapterResult.DataResult(CheckoutAdapter.adapt(it.checkout))
                    }
                }
            }
        })
    }

    override fun getAcceptedCardTypes(callback: ApiCallback<List<CardType>>) {

        val query = Storefront.query {
            it.shop { it.paymentSettings { it.acceptedCardBrands() } }
        }
        graphClient.queryGraph(query).enqueue(object : QueryCallWrapper<List<CardType>>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<List<CardType>> {
                val adaptee = data.shop.paymentSettings.acceptedCardBrands
                return AdapterResult.DataResult(CardAdapter.adapt(adaptee))
            }
        })
    }

    override fun getCardToken(card: Card, callback: ApiCallback<String>) {

        val vaultTokenUrl = object : CreditCardVaultCall.Callback {
            override fun onResponse(token: String) {
                callback.onResult(token)
            }

            override fun onFailure(error: IOException) {
                callback.onFailure(Error.Content())
            }
        }

        val vaultUrlCallback = object : ApiCallback<String> {
            override fun onResult(result: String) {
                val creditCard = CreditCard.builder()
                    .firstName(card.firstName)
                    .lastName(card.lastName)
                    .number(card.cardNumber)
                    .expireMonth(card.expireMonth)
                    .expireYear(card.expireYear)
                    .verificationCode(card.verificationCode)
                    .build()
                cardClient.vault(creditCard, result).enqueue(vaultTokenUrl)
            }

            override fun onFailure(error: Error) {
                callback.onFailure(error)
            }
        }

        val query = Storefront.query {
            it.shop {
                it.paymentSettings { it.cardVaultUrl() }
            }
        }
        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<String>(vaultUrlCallback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<String> =
                AdapterResult.DataResult(data.shop?.paymentSettings?.cardVaultUrl ?: "")
        })
    }

    override fun completeCheckoutByCard(checkout: Checkout, email: String, address: Address, creditCardVaultToken: String,
                                        callback: ApiCallback<Order>) {

        val amount = checkout.totalPrice
        val idempotencyKey = UUID.randomUUID().toString()
        val billingAddress = Storefront.MailingAddressInput()

        billingAddress.setAddress1(address.address)
            .setCity(address.city)
            .setProvince(address.state)
            .setCountry(address.country)
            .setFirstName(address.firstName)
            .setLastName(address.lastName)
            .setPhone(address.phone).zip = address.zip

        val cardPayCallback = object : ApiCallback<Boolean> {
            override fun onResult(result: Boolean) {
                completeCheckout(checkout.checkoutId, callback)
            }

            override fun onFailure(error: Error) {
                callback.onFailure(error)
            }
        }

        val creditCardPaymentInput = Storefront.CreditCardPaymentInput(amount, idempotencyKey,
            billingAddress, creditCardVaultToken)

        val mutationQuery = Storefront.mutation {
            it.checkoutEmailUpdate(ID(checkout.checkoutId), email, { it.checkout { it.totalPrice() } })
            it.checkoutCompleteWithCreditCard(ID(checkout.checkoutId), creditCardPaymentInput) {
                it.payment {
                    it.ready().errorMessage()
                }.checkout {
                    it.ready()
                }.userErrors { getDefaultUserErrors(it) }
            }
        }

        graphClient.mutateGraph(mutationQuery).enqueue(object : MutationCallWrapper<Boolean>(cardPayCallback) {
            override fun adapt(data: Storefront.Mutation?): AdapterResult<Boolean>? {
                return data?.checkoutCompleteWithCreditCard?.let {
                    val userError = ErrorAdapter.adaptUserError(it.userErrors)
                    if (userError != null) {
                        return AdapterResult.ErrorResult(userError)
                    } else {
                        if (it.checkout?.ready == true) AdapterResult.DataResult(true) else null
                    }
                }
            }
        })
    }

    /* SESSION */

    private fun saveSession(accessData: AccessData) {
        preferences.edit()
            .putString(EMAIL, accessData.email)
            .putString(ACCESS_TOKEN, accessData.accessToken)
            .putLong(EXPIRES_DATE, accessData.expiresAt)
            .apply()
    }

    private fun getSession(): AccessData? {
        val email = preferences.getString(EMAIL, null)
        val accessToken = preferences.getString(ACCESS_TOKEN, null)
        val expiresDate = preferences.getLong(EXPIRES_DATE, 0)
        val isExpired = expiresDate <= System.currentTimeMillis()
        return if (email != null && accessToken != null && !isExpired) {
            AccessData(
                email,
                accessToken,
                expiresDate
            )
        } else {
            null
        }
    }

    private fun removeSession() {
        preferences.edit()
            .remove(EMAIL)
            .remove(ACCESS_TOKEN)
            .remove(EXPIRES_DATE)
            .apply()
    }

    private fun requestToken(email: String, password: String): Pair<AccessData?, Error?>? {

        val accessTokenInput = Storefront.CustomerAccessTokenCreateInput(email, password)
        val accessTokenQuery = Storefront.CustomerAccessTokenCreatePayloadQueryDefinition { queryDefinition ->
            queryDefinition.customerAccessToken { accessTokenQuery -> accessTokenQuery.accessToken().expiresAt() }
                .userErrors { getDefaultUserErrors(it) }
        }

        val mutationQuery = Storefront.mutation { query -> query.customerAccessTokenCreate(accessTokenInput, accessTokenQuery) }
        val call = graphClient.mutateGraph(mutationQuery)
        return call.execute().data()?.customerAccessTokenCreate?.let {
            val accessData = it.customerAccessToken?.let {
                val accessData = AccessData(email, it.accessToken, it.expiresAt.millis)
                saveSession(accessData)
                accessData
            }

            val error = ErrorAdapter.adaptUserError(it.userErrors)
            Pair(accessData, error)
        }
    }

    private fun getAllCountriesList(): List<ApiCountry> {
        val countriesType = object : TypeToken<List<ApiCountry>>() {}.type
        return Gson().fromJson(assetsReader.read(COUNTRIES_FILE_NAME, context), countriesType)
    }

    private fun completeCheckout(checkoutId: String, callback: ApiCallback<Order>) {

        val query = Storefront.query {
            it.node(ID(checkoutId), {
                it.onCheckout({
                    it.order { getDefaultOrderQuery(it) }
                })
            })
        }

        val retryHandler = RetryHandler.delay(RETRY_HANDLER_DELAY, TimeUnit.MILLISECONDS)
            .maxCount(RETRY_HANDLER_MAX_COUNT)
            .whenResponse<Storefront.QueryRoot> {
                val checkout = (it.data()?.node as? Storefront.Checkout)
                checkout?.order == null
            }
            .build()

        graphClient.queryGraph(query).enqueue(object : QueryCallWrapper<Order>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<Order> {
                val checkout = data.node as? Storefront.Checkout
                val orderAdaptee = checkout?.order
                return if (orderAdaptee != null) {
                    AdapterResult.DataResult(OrderAdapter.adapt(orderAdaptee))
                } else {
                    AdapterResult.ErrorResult(Error.Content())
                }
            }
        }, null, retryHandler)
    }

    private fun queryProducts(perPage: Int, paginationValue: Any?, searchQuery: String?,
                              reverse: Boolean, sortBy: SortType?,
                              callback: ApiCallback<List<Product>>) {

        val query = Storefront.query { rootQuery ->
            rootQuery.shop { shopQuery ->
                shopQuery
                    .paymentSettings({ it.currencyCode() })
                    .products({ args ->
                        args.first(perPage)
                        if (paginationValue != null) {
                            args.after(paginationValue.toString())
                        }
                        val key = getProductSortKey(sortBy)
                        if (key != null) {
                            args.sortKey(key)
                        }
                        args.reverse(reverse)
                        if (searchQuery != null) {
                            args.query(searchQuery)
                        }
                    }
                    ) { productConnectionQuery ->
                        productConnectionQuery.edges { productEdgeQuery ->
                            productEdgeQuery.cursor().node { productQuery ->
                                getDefaultProductQuery(productQuery)
                                    .images({ it.first(1) }, { imageConnectionQuery ->
                                        imageConnectionQuery.edges({ imageEdgeQuery ->
                                            imageEdgeQuery.node({ QueryHelper.getDefaultImageQuery(it) })
                                        })
                                    })
                                    .variants({ it.first(ITEMS_COUNT) }) { productVariantConnectionQuery ->
                                        productVariantConnectionQuery.edges { productVariantEdgeQuery ->
                                            productVariantEdgeQuery.node({ it.price() })
                                        }
                                    }
                            }
                        }
                    }
            }
        }

        val call = graphClient.queryGraph(query)
        call.enqueue(object : QueryCallWrapper<List<Product>>(callback) {
            override fun adapt(data: Storefront.QueryRoot): AdapterResult<List<Product>> =
                AdapterResult.DataResult(ProductListAdapter.adapt(data.shop, data.shop.products))
        })
    }
}
