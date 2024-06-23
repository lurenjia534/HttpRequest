import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class RequestViewModel : ViewModel() {
    private val _url = MutableStateFlow("")
    val url: StateFlow<String> get() = _url

    private val _selectedMethod = MutableStateFlow("GET")
    val selectedMethod: StateFlow<String> get() = _selectedMethod

    private val _headers = MutableStateFlow("")
    val headers: StateFlow<String> get() = _headers

    private val _body = MutableStateFlow("")
    val body: StateFlow<String> get() = _body

    private val _response = MutableStateFlow("")
    val response: StateFlow<String> get() = _response

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.example.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    fun updateUrl(newUrl: String) {
        _url.value = newUrl
    }

    fun updateSelectedMethod(newMethod: String) {
        _selectedMethod.value = newMethod
    }

    fun updateHeaders(newHeaders: String) {
        _headers.value = newHeaders
    }

    fun updateBody(newBody: String) {
        _body.value = newBody
    }

    fun sendRequest() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                when (_selectedMethod.value) {
                    "GET" -> {
                        val response = apiService.getRequest(_url.value)
                        _response.value = response.body()?.string() ?: "No response"
                    }
                    "POST" -> {
                        val requestBody = RequestBody.create(null, _body.value)
                        val response = apiService.postRequest(_url.value, requestBody)
                        _response.value = response.body()?.string() ?: "No response"
                    }
                    "PUT" -> {
                        val requestBody = RequestBody.create(null, _body.value)
                        val response = apiService.putRequest(_url.value, requestBody)
                        _response.value = response.body()?.string() ?: "No response"
                    }
                    "DELETE" -> {
                        val response = apiService.deleteRequest(_url.value)
                        _response.value = response.body()?.string() ?: "No response"
                    }
                    "PATCH" -> {
                        val requestBody = RequestBody.create(null, _body.value)
                        val response = apiService.patchRequest(_url.value, requestBody)
                        _response.value = response.body()?.string() ?: "No response"
                    }
                }
            } catch (e: Exception) {
                _response.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

interface ApiService {
    @GET
    suspend fun getRequest(@Url url: String): retrofit2.Response<ResponseBody>

    @POST
    suspend fun postRequest(@Url url: String, @Body body: RequestBody): retrofit2.Response<ResponseBody>

    @PUT
    suspend fun putRequest(@Url url: String, @Body body: RequestBody): retrofit2.Response<ResponseBody>

    @DELETE
    suspend fun deleteRequest(@Url url: String): retrofit2.Response<ResponseBody>

    @PATCH
    suspend fun patchRequest(@Url url: String, @Body body: RequestBody): retrofit2.Response<ResponseBody>
}
