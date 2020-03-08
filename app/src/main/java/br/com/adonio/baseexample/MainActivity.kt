package br.com.adonio.baseexample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import br.com.adonio.promise.Promise
import br.com.adonio.promise.promise
import br.com.adonio.promise.promiseIt
import br.com.adonio.task.Task

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Task.main(2333) {
            Log.i("Adon", "s1mple")
        }

        promise<Boolean> {
            resolve(true)
        }.then {
            Log.i("Adon", "then")
        }
        .catch { Log.i("Adon", "never call 1") }
        .always {
            Log.i("Adon", "always")
        }

        promiseIt(22)
            .then {
                Log.i("Adon", "$it")
            }
            .catch { Log.i("Adon", "never call") }
            .always {
                Log.i("Adon", "always int")
            }
    }
}
