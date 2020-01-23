package com.agm91.prontoapp

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.rule.ActivityTestRule
import com.agm91.prontoapp.data.ApiResponse
import com.agm91.prontoapp.data.MapsApi
import com.agm91.prontoapp.data.PlacesRepository
import com.agm91.prontoapp.data.PlacesViewModel
import com.agm91.prontoapp.model.Places
import com.agm91.prontoapp.model.Results
import com.agm91.prontoapp.model.dagger.NetworkModule
import com.agm91.prontoapp.presentation.activity.PlacesContract
import com.agm91.prontoapp.presentation.activity.PlacesMapActivity
import com.agm91.prontoapp.presentation.activity.PlacesPresenter
import com.google.android.gms.maps.model.LatLng
import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class UnitTest {
    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @MockK
    lateinit var apiMock: MapsApi
    @MockK
    lateinit var repoMock: PlacesRepository
    @MockK
    lateinit var viewModelMock: PlacesViewModel
    @Mock
    var view: PlacesContract.View? = null

    @Inject
    lateinit var apiDagger: MapsApi
    @Inject
    lateinit var repoDagger: PlacesRepository
    @Inject
    lateinit var viewModelDagger: PlacesViewModel
    @Inject
    lateinit var presenterDagger: PlacesPresenter

    val net = NetworkModule()
    lateinit var api: MapsApi
    lateinit var repo: PlacesRepository
    lateinit var viewModel: PlacesViewModel
    lateinit var presenter: PlacesPresenter

    private val type = "restaurant"
    private val location = "19.406799052955666,-99.17740277945995"

    private val devicesLiveData:
            MutableLiveData<ApiResponse<Places>> = MutableLiveData()

    @Rule
    @JvmField
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Rule
    @JvmField
    var rule = ActivityTestRule(PlacesMapActivity::class.java)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        val component = DaggerTestActivityComponent.builder()
            .networkModule(TestNetworkModule())
            .activityModule(TestActivityModule(rule.activity))
            .build()
        component.into(this)


        val retrofit = net.provideRetrofit()
        api = net.provideMapsApi(retrofit)
        repo = PlacesRepository(api)
        viewModel = PlacesViewModel(repo)
        presenter = PlacesPresenter(viewModel, rule.activity)

        assertNotNull(api)
        assertNotNull(viewModel)
        assertNotNull(repo)
        assertNotNull(presenter)

        presenter.currentLatLng = LatLng(19.406799052955666, -99.17740277945995)
    }

    @Test
    fun `testing dagger injection`() {
        assertNotNull(apiDagger)
        assertNotNull(viewModelDagger)
        assertNotNull(repoDagger)
        assertNotNull(presenterDagger)
    }

    @Test
    fun `testing Mockk`() {
        assertNotNull(apiMock)
        assertNotNull(viewModelMock)
        assertNotNull(repoMock)
    }

    @Test
    fun `testing viewModel`() {
        viewModel.getPlaces(type, location, 1000.toDouble()).observeOnce {
            println("Test: " + it.toString())
            Truth.assertThat(it.data != null || it.error != null)
        }
        Truth.assertThat(viewModel.getPlaces(type, location, 1000.toDouble()).hasObservers())
    }

    @Test
    fun `testing presenter`() {
        presenter.onViewModel(type)
        Thread.sleep(2000)
        val markers = presenterDagger.getMarkers()
        Truth.assertThat(markers)
        markers.forEach {
            Truth.assertThat(it)
            Truth.assertThat(it.tag != null)
            val place: Results? = it.tag as Results?
            Truth.assertThat(place)
        }
    }
}
