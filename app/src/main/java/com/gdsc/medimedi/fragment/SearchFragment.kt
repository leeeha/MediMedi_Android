package com.gdsc.medimedi.fragment

import android.annotation.SuppressLint
import android.graphics.Point
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.gdsc.medimedi.GraphicOverlay
import com.gdsc.medimedi.databinding.FragmentSearchBinding
import com.gdsc.medimedi.retrofit.RESTApi
import com.gdsc.medimedi.retrofit.SearchRequest
import com.gdsc.medimedi.retrofit.SearchResponse
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.Executors

// 히스토리 화면에서 카메라 화면으로 돌아온 뒤에 뒤로가기 누르면,
// 홈 화면으로 가기 전에 앱 종료됨. 프래그먼트 not attached 에러.
class SearchFragment : Fragment(), TextToSpeech.OnInitListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val args: SearchFragmentArgs by navArgs()
    private val TAG: String = "CameraX Debug"

    private lateinit var navController : NavController
    private lateinit var tts: TextToSpeech

    private lateinit var objectDetector: ObjectDetector

    // Future? 비동기 연산 결과를 리턴하는 객체 (완료, 취소)
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>

    // ProcessCameraProvider? Used to bind the lifecycle of cameras to the lifecycle owner
    private lateinit var cameraProvider: ProcessCameraProvider

    // 카메라 관련 옵션 설정 (카메라 방향, 필터링 등)
    private lateinit var cameraSelector: CameraSelector

    // Use case
    private lateinit var previewUseCase: Preview
    private lateinit var analysisUseCase: ImageAnalysis
    private lateinit var captureUseCase: ImageCapture

    private val boxList = listOf("Packaged goods", "Box", "Business card", "Container")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        tts = TextToSpeech(requireContext(), this)

        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // Camera provider is now guaranteed to be available
            cameraProvider = cameraProviderFuture.get()

            // LifecycleOwner의 생명주기에 카메라 프리뷰를 바인딩 시킨다.
            bindPreview(cameraProvider)

        }, ContextCompat.getMainExecutor(requireContext()))
        // executor는 future의 비동기 처리 작업이 끝났을 때, 완료 리스너를 호출한다.

        // 커스텀 모델 생성
        val localModel = LocalModel.Builder()
            .setAssetFilePath("custom_models/object_labeler_sample.tflite")
            .build()

        // 객체 탐지 옵션 설정
        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f) // 신뢰도가 50퍼센트 넘으면 감지된 객체 리턴
                .setMaxPerObjectLabelCount(3) // 객체 하나당 붙일 수 있는 라벨의 최대 개수
                .build()

        // 객체 탐지기 생성
        objectDetector =
            ObjectDetection.getClient(customObjectDetectorOptions)

        // 검색 기록 조회 버튼
        binding.btnHistory.setOnClickListener{
            val action = SearchFragmentDirections.actionSearchFragmentToHistoryFragment("검색 기록 조회하기")
            findNavController().navigate(action)
        }

        // 촬영 버튼 (약 상자처럼 생긴 물체가 감지되면 촬영)
        binding.btnCamera.setOnClickListener{
            takePhoto()
        }
    }

    // tts 객체 초기화
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.KOREA
            tts.setPitch(0.6F)
            tts.setSpeechRate(1.2F)
            speakOut(args.tts.toString())
        } else {
            Log.e("TTS", "Initialization Failed!")
        }
    }

    private fun speakOut(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        // 카메라 옵션 설정
        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        // 1. Set up the preview use case to display camera preview.
        previewUseCase = Preview.Builder().build()

        // Connect the preview use case to the previewView
        previewUseCase.setSurfaceProvider(binding.previewView.surfaceProvider)

        // 2. Set up the capture use case to allow users to take photos.
        captureUseCase = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY) // 지연 시간 최소화 모드
            .build()

        // 3. Set up the analysis use case to analyze camera preview.
        val point = Point()
        analysisUseCase = ImageAnalysis.Builder()
            // 디바이스의 화면 해상도 전달
            .setTargetResolution(Size(point.x, point.y))
            // 카메라가 너무 빨리 움직일 때 가장 최신 상태의 프레임만 얻는다.
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        // imageAnalysis를 ImageAnalyzer에게 전달하면, imageProxy가 리턴되어 카메라로부터 이미지를 얻고,
        // setAnalyzer의 매개변수로 전달된 executor는 imageProxy를 실행시킨다.
        analysisUseCase.setAnalyzer(ContextCompat.getMainExecutor(requireContext())) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = imageProxy.image
            if (image != null) {
                // media.Image 타입의 입력 이미지 얻기
                val inputImage = InputImage.fromMediaImage(image, rotationDegrees)

                // objectDetector가 입력 이미지를 처리한다.
                objectDetector
                    .process(inputImage)
                    .addOnFailureListener {
                        // 객체 탐지에 성공하든 실패하든 imageProxy를 close 해줘야, 다음 이미지를 처리할 수 있음.
                        imageProxy.close()
                    }.addOnSuccessListener { objects ->
                        // a list of objects which are detected.
                        for(it in objects) {
                            // If the child count is greater than 1 then the rectangle view is already drawn.
                            if(binding.layout.childCount > 1)
                            // If the view is already drawn then we will remove it.
                                binding.layout.removeViewAt(1)

                            // Undefined여도 어떤 객체가 감지되긴 한 거니까 캡처 범위에 포함시키자.
                            val objectLabel = it.labels.firstOrNull()?.text ?: "Undefined"
                            val element = GraphicOverlay(requireContext(), it.boundingBox, objectLabel)

                            // After removing, we will add a new rectangle view to the parent view.
                            binding.layout.addView(element,1)

                            // 약 상자처럼 생긴 물체가 감지될 경우, 이미지 캡처하기
                            if(boxList.contains(objectLabel)){
                                takePhoto()
                            }
                        }
                        imageProxy.close()
                    }
            }
        }

        try {
            // Unbind use cases before rebinding
            cameraProvider.unbindAll()

            // Attach three use cases to the camera with the same lifecycle owner
            cameraProvider.bindToLifecycle(
                this as LifecycleOwner, cameraSelector, previewUseCase, analysisUseCase, captureUseCase)

        } catch(exc: Exception) {
            Log.e("bindToLifecycle", "Use case binding failed", exc)
        }
    }

    private fun takePhoto() {
        // /storage/sdcard0/Android/data/package/files
        val file = File(requireActivity().getExternalFilesDir(null)?.absolutePath,
            System.currentTimeMillis().toString() + ".jpg")
        file.createNewFile()
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        captureUseCase.takePicture(outputFileOptions,
            Executors.newSingleThreadExecutor(),
            object: ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    // runOnUiThread? fragment host(=Activity)의 메인 쓰레드(=UI 쓰레드)에서 호출돼야 한다.
                    requireActivity().runOnUiThread{
                        Log.d(TAG, "Error: ${exception.message}")
                    }
                }
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    requireActivity().runOnUiThread{
                        val imgUri = outputFileResults.savedUri
                        Log.d(TAG, "Saved successfully: $imgUri")

                        // 결과 화면으로 이미지 주소 넘기기
                        val action = SearchFragmentDirections.actionSearchFragmentToResultFragment(imgUri.toString())
                        findNavController().navigate(action)
                    }
                }
            })
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        tts.stop()
//        tts.shutdown()
//        _binding = null
//    }
}