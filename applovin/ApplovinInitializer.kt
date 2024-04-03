package io.bidapp.networks.applovin

import android.content.Context
import com.applovin.sdk.AppLovinSdk
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol
import io.bidapp.sdk.utils.dispatch_main
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException

@PublishedApi
internal object ApplovinInitializer : IApplovinInitializer {
    private val startSDKWaiters = arrayListOf<WeakReference<BIDNetworkAdapterProtocol>>()

    fun doStart(listener: BIDNetworkAdapterProtocol, context: Context, appId: String?) {
           startSDKWaiters.add(WeakReference(listener))
        if (1 == startSDKWaiters.size) {
            BIDApplovinSDK.appId = appId
            BIDApplovinSDK.appLovinGetInstanceSDK(context.applicationContext).mediationProvider = "max"
            BIDApplovinSDK.appLovinGetInstanceSDK(context.applicationContext).initializeSdk {
                val success = BIDApplovinSDK.appLovinGetInstanceSDK(context.applicationContext).isInitialized
                dispatch_main {
                    if (!success) {
                        startSDKWaiters.forEach {
                            it.get()?.onInitializationComplete(
                                false,
                                "ApplovinMAX isInitialized failed"
                            )
                        }
                        return@dispatch_main
                    }
                    startSDKWaiters.forEach {
                        it.get()?.onInitializationComplete(true, null)
                    }
                    startSDKWaiters.clear()
                }
            }
        }
    }

    override fun start(listener: BIDNetworkAdapterProtocol, context: Context, appId: String?) {
        val applovinInitializerMax = applovinInitializerMax()
        val applovinInitializer = applovinInitializer()
        if (applovinInitializerMax && applovinInitializer) runStartForClass("io.bidapp.networks.applovinmax.ApplovinInitializerMax", listener, context, appId)
        else if (applovinInitializerMax) runStartForClass("io.bidapp.networks.applovinmax.ApplovinInitializerMax", listener, context, appId)
        else runStartForClass("io.bidapp.networks.applovin.ApplovinInitializer", listener, context, appId)
    }

    private fun applovinInitializerMax(): Boolean {
        return try {
            Class.forName("io.bidapp.networks.applovinmax.ApplovinInitializerMax")
            true
        } catch (ex: ClassNotFoundException) {
            false
        }
    }

    private fun applovinInitializer(): Boolean {
        return try {
            Class.forName("io.bidapp.networks.applovin.ApplovinInitializer")
            true
        } catch (ex: ClassNotFoundException) {
            false
        }
    }

    private fun runStartForClass(cl:String, adapterProtocol : BIDNetworkAdapterProtocol, context: Context, appId: String?){
        try {
            val className = cl
            val methodName = "doStart"
            val parameterTypes = arrayOf(BIDNetworkAdapterProtocol::class.java, Context::class.java, String::class.java)
            val parameters = arrayOf(adapterProtocol, context, appId)
            val clazz = Class.forName(className)
            val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
            val instance = clazz.getField("INSTANCE").get(null)
            method.invoke(instance, *parameters)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            println("Applovin initialization - ClassNotFoundException")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            println("Applovin initialization - NoSuchMethodException")
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            println("Applovin initialization - IllegalAccessException")
        } catch (e: InstantiationException) {
            e.printStackTrace()
            println("Applovin initialization - InstantiationException")
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            println("Applovin initialization - InvocationTargetException")
        }
    }
}

