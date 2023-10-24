package io.bidapp.networks.applovin

import android.app.Activity
import android.util.Log
import com.applovin.sdk.AppLovinSdk
import io.bidapp.sdk.utils.dispatch_main
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException

@PublishedApi
internal object ApplovinInitializer : IApplovinInitializer {
    val startSDKWaiters = arrayListOf<WeakReference<BIDNetworkAdapterProtocol>>()

    fun doStart(listener: BIDNetworkAdapterProtocol, activity: Activity) {
        startSDKWaiters.add(WeakReference(listener))
        if (1 == startSDKWaiters.size) {

            AppLovinSdk.getInstance(activity).mediationProvider = "max"
            AppLovinSdk.getInstance(activity).initializeSdk {
                val success = AppLovinSdk.getInstance(activity).isInitialized
                dispatch_main {
                    if (success) {
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

    override fun start(listener: BIDNetworkAdapterProtocol, activity: Activity) {
        val applovinInitializerMax = applovinInitializerMax()
        val applovinInitializer = applovinInitializer()
        if (applovinInitializerMax && applovinInitializer) runStartForClass("io.bidapp.networks.applovinmax.ApplovinInitializerMax", listener, activity)
        else if (applovinInitializerMax) runStartForClass("io.bidapp.networks.applovinmax.ApplovinInitializerMax", listener, activity)
        else runStartForClass("io.bidapp.networks.applovin.ApplovinInitializer", listener, activity)
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

    private fun runStartForClass(cl:String, adapterProtocol : BIDNetworkAdapterProtocol, activity: Activity){
        try {
            val className = cl
            val methodName = "doStart"
            val parameterTypes = arrayOf(BIDNetworkAdapterProtocol::class.java, Activity::class.java)
            val parameters = arrayOf(adapterProtocol, activity)
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
