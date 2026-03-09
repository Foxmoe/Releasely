import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class ApiService {
  final Dio _dio = Dio(BaseOptions(
    baseUrl: 'http://localhost:8080/api', // 改为你的后端地址, Android模拟器用10.0.2.2
    connectTimeout: const Duration(seconds: 5),
    receiveTimeout: const Duration(seconds: 3),
  ));
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  ApiService() {
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await _storage.read(key: 'auth_token');
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        return handler.next(options);
      },
    ));
  }

  Future<String?> login(String username, String password) async {
    try {
      final response = await _dio.post('/auth/login', data: {
        'username': username,
        'password': password,
      });
      final token = response.data['token'];
      await _storage.write(key: 'auth_token', value: token);
      return null; // Success
    } on DioException catch (e) {
       if (e.response != null) {
         return 'Login failed: ${e.response?.statusCode}';
       }
      return 'Network error: ${e.message}';
    }
  }

  Future<String?> register(String username, String password, String email) async {
    try {
      await _dio.post('/auth/register', data: {
        'username': username,
        'password': password,
        'email': email,
      });
      return null;
    } on DioException catch (e) {
      if (e.response != null) {
         return 'Registration failed: ${e.response?.data ?? e.response?.statusCode}'; // 简单处理
       }
      return 'Network error: ${e.message}';
    }
  }

  Future<void> logout() async {
    await _storage.delete(key: 'auth_token');
  }

  Future<bool> isLoggedIn() async {
    return await _storage.containsKey(key: 'auth_token');
  }

  // Activity API Wrappers
  Future<void> addActivity(Map<String, dynamic> data) async {
      await _dio.post('/v1/activity/add', data: data);
  }
}
