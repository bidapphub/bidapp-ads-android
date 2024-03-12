package io.bidapp.networks.applovinmax

import android.content.Context
import io.bidapp.sdk.protocols.BIDNetworkAdapterProtocol
import io.bidapp.sdk.utils.dispatch_main
import java.lang.ref.WeakReference
import java.lang.reflect.InvocationTargetException


@PublishedApi
internal object ApplovinInitializerMax : IApplovinInitializer {
    val startSDKWaiters = arrayListOf<WeakReference<BIDNetworkAdapterProtocol>>()


    fun doStart(listener: BIDNetworkAdapterProtocol, context: Context) {
          startSDKWaiters.add(WeakReference(listener))
         if (1 == startSDKWaiters.size) {
            BIDApplovinMaxSDK.appLovinGetMaxInstanceSDK(context.applicationContext).mediationProvider = "max"
            BIDApplovinMaxSDK.appLovinGetMaxInstanceSDK(context.applicationContext).initializeSdk {
                val success = BIDApplovinMaxSDK.appLovinGetMaxInstanceSDK(context.applicationContext).isInitialized
                dispatch_main {
                    if (!success) {
                        startSDKWaiters.forEach {
                            it.get()?.onInitializationComplete(
                                false,
                                "ApplovinMAX initialized failed"
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

    override fun start(listener: BIDNetworkAdapterProtocol, context: Context) {
        val applovinInitializerMax = applovinInitializerMax()
        val applovinInitializer = applovinInitializer()
        if (applovinInitializerMax && applovinInitializer) runStartForClass("io.bidapp.networks.applovinmax.ApplovinInitializerMax", listener, context)
        else if (applovinInitializerMax) runStartForClass("io.bidapp.networks.applovinmax.ApplovinInitializerMax", listener, context)
        else runStartForClass("io.bidapp.networks.applovin.ApplovinInitializer", listener, context)
    }

    fun applovinInitializerMax(): Boolean {
        return try {
            Class.forName("io.bidapp.networks.applovinmax.ApplovinInitializerMax")
            true
        } catch (ex: ClassNotFoundException) {
            false
        }
    }

    fun applovinInitializer(): Boolean {
        return try {
            Class.forName("io.bidapp.networks.applovin.ApplovinInitializer")
            true
        } catch (ex: ClassNotFoundException) {
            false
        }
    }

    private fun runStartForClass(cl:String, adapterProtocol : BIDNetworkAdapterProtocol, context: Context){
        try {
            val className = cl
            val methodName = "doStart"
            val parameterTypes = arrayOf(BIDNetworkAdapterProtocol::class.java, Context::class.java)
            val parameters = arrayOf(adapterProtocol, context)
            val clazz = Class.forName(className)
            val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
            val instance = clazz.getField("INSTANCE").get(null)
            method.invoke(instance, *parameters)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            println("Applovin max initialization - ClassNotFoundException")

        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            println("Applovin max initialization - NoSuchMethodException")

        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            println("Applovin max initialization - IllegalAccessException")

        } catch (e: InstantiationException) {
            e.printStackTrace()
            println("Applovin max initialization - InstantiationException")

        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            println("Applovin max initialization - InvocationTargetException")
        }
    }
}

