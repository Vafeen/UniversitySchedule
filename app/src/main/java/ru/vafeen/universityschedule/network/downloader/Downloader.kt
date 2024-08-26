package ru.vafeen.universityschedule.network.downloader

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.vafeen.universityschedule.network.repository.NetworkRepository
import java.io.File
import java.io.FileOutputStream


object Downloader {
    val sizeFlow = MutableSharedFlow<Progress>()
    val isUpdateInProcessFlow = MutableSharedFlow<Boolean>()
    fun installApk(context: Context, apkFilePath: String) {
        // Создаем объект File для APK файла по указанному пути
        val file = File(apkFilePath)

        // Проверяем, существует ли файл
        if (file.exists()) {
            // Создаем Intent для установки APK
            val intent = Intent(Intent.ACTION_VIEW).apply {
                // Устанавливаем URI и MIME-тип для файла
                setDataAndType(
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider", // Указываем авторитет FileProvider
                        file
                    ),
                    "application/vnd.android.package-archive" // MIME-тип для APK файлов
                )
                // Добавляем флаг для предоставления разрешения на чтение URI
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Добавляем флаг для запуска новой задачи
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            // Запускаем активность для установки APK
            context.startActivity(intent)
        } else {
            // Логируем ошибку, если файл не существует
            Log.e("InstallApk", "APK file does not exist: $apkFilePath")
        }
    }

    fun downloadApk(
        networkRepository: NetworkRepository,
        url: String, filePath: String
    ) {
        // Создаем вызов для загрузки файла
        val call = networkRepository.downloadFile(url)

        // Выполняем асинхронный запрос
        call?.enqueue(object : Callback<ResponseBody> {
            // Обрабатываем успешный ответ
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                // Запускаем корутину для выполнения операции ввода-вывода
                CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
                    try {
                        // Проверяем, успешен ли ответ
                        if (response.isSuccessful) {
                            response.body()?.let { body ->
                                // Создаем файл для записи данных
                                val file = File(filePath)
                                // Получаем поток данных из тела ответа
                                val inputStream = body.byteStream()
                                // Создаем поток для записи данных в файл
                                val outputStream = FileOutputStream(file)
                                // Буфер для чтения данных
                                val buffer = ByteArray(8 * 1024)
                                var bytesRead: Int
                                var totalBytesRead: Long = 0
                                // Получаем длину содержимого
                                val contentLength = body.contentLength()

                                // Используем потоки для чтения и записи данных
                                inputStream.use { input ->
                                    outputStream.use { output ->
                                        while (input.read(buffer).also { bytesRead = it } != -1) {
                                            // запись данных из буфера в выходной поток
                                            output.write(buffer, 0, bytesRead)
                                            totalBytesRead += bytesRead
                                            // Отправляем прогресс загрузки
                                            sizeFlow.emit(
                                                Progress(
                                                    totalBytesRead = totalBytesRead,
                                                    contentLength = contentLength,
                                                    done = totalBytesRead == contentLength
                                                )
                                            )
                                        }
                                    }
                                }
                                // Логируем успешную загрузку
                                Log.d("status", "Downloaded")
                            }
                        } else {
                            // Логируем ошибку при неуспешном ответе
                            Log.e("status", "Failed to download file")
                        }
                    } catch (e: Exception) {
                        // Обрабатываем исключение и отправляем сигнал о неудаче
                        CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
                            sizeFlow.emit(Progress(failed = true))
                        }
                    }
                }
            }

            // Обрабатываем ошибку при выполнении запроса
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("status", "Download error: ${t.message}")
            }
        })
    }

}