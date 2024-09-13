package com.example.myapplicationlast

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okio.BufferedSink
import okio.buffer
import okio.sink
import org.json.JSONObject
import java.io.File
import java.io.IOException


class success : AppCompatActivity() {

    private var annotatedVideoPath: String = ""
    private val PICK_VIDEO_REQUEST = 1
    private lateinit var btnUploadVideo: Button
    private lateinit var treatmentBtn: Button
    private lateinit var videoPath: String

    private lateinit var videoView: VideoView
    private var treatmentClicked = false
    private lateinit var outputPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)

        // Initialiser les boutons
        btnUploadVideo = findViewById(R.id.btnUploadVideo)
        treatmentBtn = findViewById(R.id.treatmentBtn)
        videoView = findViewById(R.id.videoView)

        // Bouton pour uploader la vidéo
        btnUploadVideo.setOnClickListener {
            openFilePicker()

        }

        // Bouton pour demander le traitement de la vidéo
        treatmentBtn.setOnClickListener {
            // Définir treatmentClicked sur true lorsque le bouton est cliqué
            treatmentClicked = true
            // Vérifier si le traitement est terminé
            if (outputPath != null) {
                // Charger la vidéo traitée dans le VideoView
                videoView.setVideoPath(outputPath)
                videoView.visibility = android.view.View.VISIBLE
                videoView.start()
            } else {
                // Afficher un message d'erreur à l'utilisateur
                Toast.makeText(
                    applicationContext,
                    "La vidéo n'est pas encore traitée",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "video/*"
        startActivityForResult(intent, PICK_VIDEO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val videoUri: Uri = data.data!!
            System.out.println("video path uri" + videoUri)
            // Resolve the actual file path from the URI
            val filePath = getRealPathFromUri(videoUri)
            System.out.println("filepath = " + filePath)
            if (filePath != null) {
                videoPath = filePath

                System.out.println("video path equal to " + filePath)

                // Proceed with displaying and uploading the video
                videoView.setVideoURI(videoUri)
                videoView.visibility = android.view.View.VISIBLE
                videoView.start()
                uploadVideo(videoPath)
            } else {
                // Handle the case where file path resolution fails
                Toast.makeText(
                    applicationContext,
                    "Failed to retrieve video file path",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getRealPathFromUri(uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                filePath = it.getString(columnIndex)
            }
        }
        return filePath
    }


    private fun uploadVideo(videoPath: String) {
        print("Start uploading video")
        val file = File(videoPath)
        //utilisée dans la construction de la requête HTTP pour envoyer le fichier vidéo au serveur.
        val requestFile = file.asRequestBody("video/mp4".toMediaTypeOrNull())
        //prépare les données à envoyer au serveur Flask sous la forme d'un fichier vidéo dans un corps de requête multipart.
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("video", file.name, requestFile)
            .build()
        //crée une requête HTTP POST avec le contenu de la vidéo à envoyer au serveur Flask pour l'upload.
        val request = Request.Builder()
            .url("http://192.168.1.12:5000/upload_video")
            .post(body)
            .build()
        //crée une instance d'un client HTTP OkHttp, qui est utilisée pour effectuer des appels réseau.
        val client = OkHttpClient()
        //envoie la requête de manière asynchrone et attend une réponse (enqueue)
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Gérer l'échec de l'upload

                runOnUiThread {
                    // Afficher un message d'erreur à l'utilisateur
                    Toast.makeText(
                        applicationContext,
                        "Échec de l'upload de la vidéooo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                // Gérer la réponse de l'upload
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody)
                    val uuid = json.getString("uuid") // Récupérer l'UUID de la réponse
                    print("uiid is " + uuid)
                    var responseStatus = true
                    runOnUiThread {
                        // Afficher un message de réussite à l'utilisateur
                        Toast.makeText(
                            applicationContext,
                            "La vidéo a été uploadée avec succès",
                            Toast.LENGTH_SHORT
                        ).show()
                        getVideo(uuid)
                    }
                } else {
                    // Gérer l'échec de l'upload
                    runOnUiThread {
                        // Afficher un message d'erreur à l'utilisateur
                        Toast.makeText(
                            applicationContext,
                            "Échec de l'upload de la vidéo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        })
    }

    private fun getVideo(uiid: String) {
        print("GEt video with uuid "+ uiid)
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("http://192.168.1.12:5000/annotated_video?uiid=$uiid")
            .get()
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // Gérer l'échec du traitement
                runOnUiThread {
                    // Afficher un message d'erreur à l'utilisateur
                    Toast.makeText(
                        applicationContext,
                        "Échec du traitement de la vidéo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                // Gérer la réponse du traitement
                if (response.isSuccessful) {
                    //récupère le corps de la réponse HTTP en tant que chaîne de caractères.
                    val responseBody = response.body
                    print("response = " + response)
                    // Analyser la réponse du serveur, par exemple, extraire le chemin de la vidéo traitée
                    if(!response.body!!.contentType().toString()!!.equals("video/mp4")){
                        // Si la vidéo n'est pas encore disponible, planifier une nouvelle vérification après un certain délai
                        //crée un nouvel objet Handler associé au thread principal (UI thread) de l'application Android.
                        // Le thread principal est responsable de toutes les interactions avec l'interface utilisateur de l'application.
                        Thread.sleep(5000)
                        getVideo(uiid)
                    }else {
                        var file = File("/storage/emulated/0/Download/video_$uiid.mp4")
                        outputPath=file.path
                        if(!file.exists())
                            file.createNewFile()

                        val sink: BufferedSink = file.sink().buffer()
                        sink.writeAll(responseBody!!.source())
                        sink.close()
                        runOnUiThread {
                            // Mettre à jour le chemin de la vidéo traitée
                            this@success.annotatedVideoPath = annotatedVideoPath
                            // Afficher un message de réussite à l'utilisateur
                            Toast.makeText(
                                applicationContext,
                                "La vidéo a été traitée avec succès",
                                Toast.LENGTH_SHORT
                            ).show()
                            // Charger la vidéo traitée dans le VideoView si le bouton "treatment" est cliqué

                        }
                    }
                } else {
                    // Gérer l'échec du traitement
                    runOnUiThread {
                        // Afficher un message d'erreur à l'utilisateur
                        Toast.makeText(
                            applicationContext,
                            "Échec du traitement de la vidéo",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

}
