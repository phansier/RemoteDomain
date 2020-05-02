import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.beryukhov.reactivenetwork.ReactiveNetwork

actual class RN(private val context: Context) {
    actual fun observeNetworkConnectivity(): Flow<RNConnectivity> {
        return ReactiveNetwork.create().observeNetworkConnectivity(context).map{
            RNConnectivity(it.available())
        }
    }
}