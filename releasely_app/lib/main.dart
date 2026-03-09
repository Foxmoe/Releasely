import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:intl/intl.dart';

import 'api_service.dart';
import 'screens/home_screen.dart'; // Import HomeScreen

void main() {
  runApp(const ReleaselyApp());
}

class ReleaselyApp extends StatelessWidget {
  const ReleaselyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Releasely',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        fontFamily: 'Roboto', // 如果有自定义字体更好
        scaffoldBackgroundColor: const Color(0xFFF5F7FA), // 浅灰蓝背景
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF6B4EFF), // 鲜艳紫
          primary: const Color(0xFF6B4EFF),
          surface: Colors.white,
        ),
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.transparent,
          elevation: 0,
          centerTitle: true,
          titleTextStyle: TextStyle(
            color: Color(0xFF2D3436),
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
          iconTheme: IconThemeData(color: Color(0xFF2D3436)),
        ),
        cardTheme: CardThemeData(
          elevation: 0,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          color: Colors.white,
          margin: const EdgeInsets.symmetric(vertical: 8),
        ),
        inputDecorationTheme: InputDecorationTheme(
          filled: true,
          fillColor: const Color(0xFFF5F7FA),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(16),
            borderSide: BorderSide.none,
          ),
          contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        ),
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color(0xFF6B4EFF),
            foregroundColor: Colors.white,
            elevation: 8,
            shadowColor: const Color(0xFF6B4EFF).withValues(alpha: 0.4),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
            textStyle: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
          ),
        ),
        filledButtonTheme: FilledButtonThemeData(
           style: FilledButton.styleFrom(
             shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
             padding: const EdgeInsets.symmetric(vertical: 18),
           )
        )
      ),
      home: const HomeScreen(),
    );
  }
}

class LoginPage extends StatefulWidget {
  const LoginPage({super.key});

  @override
  State<LoginPage> createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _usernameController = TextEditingController();
  final _passwordController = TextEditingController();
  final _api = ApiService();

  Future<void> _login() async {
      // 简单模拟，实际应调用 _api.login
      final error = await _api.login(_usernameController.text, _passwordController.text);
      if (error == null) {
        if (!mounted) return;
        Navigator.of(context).pushReplacement(
            MaterialPageRoute(builder: (context) => const HomeScreen())); // Navigate to HomeScreen instead of RecordPage
      } else {
         if (!mounted) return;
         ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(error)));
      }
  }

    @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(title: const Text('Login Releasely')),
            body: Padding(
                padding: const EdgeInsets.all(16.0),
        child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
                TextField(controller: _usernameController, decoration: const InputDecoration(labelText: 'Username')),
                TextField(controller: _passwordController, decoration: const InputDecoration(labelText: 'Password'), obscureText: true,),
                const SizedBox(height: 20),
                FilledButton(onPressed: _login, child: const Text('Login')),
                TextButton(onPressed: () {
                    // Navigate to register
                }, child: const Text('No account? Register'))
            ],
        ),
    ));
  }
}

class RecordPage extends StatefulWidget {
  const RecordPage({super.key});

  @override
  State<RecordPage> createState() => _RecordPageState();
}

class _RecordPageState extends State<RecordPage> {
  // 表单状态
  String _selectedType = 'MASTURBATION';
  double _pleasureRating = 3.0;
  DateTime _selectedDate = DateTime.now();
  final TextEditingController _noteController = TextEditingController();

  final List<Map<String, String>> _types = [
    {'value': 'MASTURBATION', 'label': '自慰'},
    {'value': 'PARTNER_SEX', 'label': '伴侣性行为'},
    {'value': 'EDGING', 'label': '边缘性行为'},
  ];

  Future<void> _submitData() async {
    // 这里应调用 Dio 或 Http进行网络请求，或者存入本地 SQLite/Hive
    // TODO: 实现本地存储与同步逻辑

    final record = {
      "type": _selectedType,
      "rating": _pleasureRating.toInt(),
      "occurredAt": _selectedDate.toIso8601String(),
      "note": _noteController.text,
    };

    // 模拟提交
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('已记录: ${record['type']} - ${record['rating']}分')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBodyBehindAppBar: true, // 让内容延伸到顶部
      appBar: AppBar(title: const Text('今日记录')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(20, 100, 20, 100), // 为顶部AppBar和底部导航留位
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildSectionHeader('行为类型', Icons.category_outlined),
            const SizedBox(height: 12),
            Container(
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.04),
                    blurRadius: 20,
                    offset: const Offset(0, 10),
                  ),
                ],
              ),
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 5),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<String>(
                  isExpanded: true,
                  value: _selectedType,
                  icon: const Icon(Icons.keyboard_arrow_down_rounded, color: Color(0xFF6B4EFF)),
                  style: const TextStyle(fontSize: 16, color: Color(0xFF2D3436), fontWeight: FontWeight.w500),
                  items: _types.map((e) {
                    return DropdownMenuItem(
                        value: e['value'], child: Text(e['label']!));
                  }).toList(),
                  onChanged: (v) => setState(() => _selectedType = v!),
                ),
              ),
            ),

            const SizedBox(height: 24),
            _buildSectionHeader('愉悦指数', Icons.sentiment_satisfied_alt_rounded),
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(24),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.04),
                    blurRadius: 20,
                    offset: const Offset(0, 10),
                  ),
                ],
              ),
              child: Column(
                children: [
                   Row(
                     mainAxisAlignment: MainAxisAlignment.spaceBetween,
                     children: [
                       Text('Rating', style: TextStyle(color: Colors.grey[400], fontWeight: FontWeight.w600)),
                       Container(
                         padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                         decoration: BoxDecoration(
                           color: const Color(0xFF6B4EFF).withValues(alpha: 0.1),
                           borderRadius: BorderRadius.circular(12),
                         ),
                         child: Text(
                           _pleasureRating.toInt().toString(),
                           style: const TextStyle(
                             color: Color(0xFF6B4EFF),
                             fontWeight: FontWeight.bold,
                             fontSize: 18,
                           ),
                         ),
                       ),
                     ],
                   ),
                   SliderTheme(
                    data: SliderThemeData(
                      activeTrackColor: const Color(0xFF6B4EFF),
                      inactiveTrackColor: const Color(0xFFF0F0F0),
                      thumbColor: Colors.white,
                      thumbShape: const RoundSliderThumbShape(enabledThumbRadius: 12.0, disabledThumbRadius: 12.0),
                      trackHeight: 8,
                      overlayColor: const Color(0xFF6B4EFF).withValues(alpha: 0.1),
                    ),
                    child: Slider(
                      value: _pleasureRating,
                      min: 1,
                      max: 5,
                      divisions: 4,
                      onChanged: (v) => setState(() => _pleasureRating = v),
                    ),
                  ),
                  const Padding(
                    padding: EdgeInsets.symmetric(horizontal: 10),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text('😩', style: TextStyle(fontSize: 20)),
                        Text('😊', style: TextStyle(fontSize: 20)),
                        Text('😍', style: TextStyle(fontSize: 20)),
                        Text('🤯', style: TextStyle(fontSize: 20)),
                        Text('😇', style: TextStyle(fontSize: 20)),
                      ],
                    ),
                  )
                ],
              ),
            ),

            const SizedBox(height: 24),
            _buildSectionHeader('发生时间', Icons.access_time_rounded),
            const SizedBox(height: 12),
            GestureDetector(
              onTap: () async {
                 final date = await showDatePicker(
                    context: context,
                    initialDate: _selectedDate,
                    firstDate: DateTime(2020),
                    lastDate: DateTime.now(),
                    builder: (context, child) {
                      return Theme(
                        data: ThemeData.light().copyWith(
                          colorScheme: const ColorScheme.light(primary: Color(0xFF6B4EFF)),
                        ),
                        child: child!,
                      );
                    },
                );
                if (date != null) {
                   if (!context.mounted) return;
                   final time = await showTimePicker(
                     context: context,
                     initialTime: TimeOfDay.fromDateTime(_selectedDate),
                      builder: (context, child) {
                      return Theme(
                        data: ThemeData.light().copyWith(
                          colorScheme: const ColorScheme.light(primary: Color(0xFF6B4EFF)),
                        ),
                        child: child!,
                      );
                    },
                   );
                   if (time != null) {
                     setState(() {
                       _selectedDate = DateTime(date.year, date.month, date.day, time.hour, time.minute);
                     });
                   }
                }
              },
              child: Container(
                padding: const EdgeInsets.all(20),
                decoration: BoxDecoration(
                  color: Colors.white,
                  borderRadius: BorderRadius.circular(20),
                  boxShadow: [
                    BoxShadow(color: Colors.black.withValues(alpha: 0.04), blurRadius: 20, offset: const Offset(0, 10)),
                  ],
                ),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: const Color(0xFFFFF0F0),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Icon(Icons.calendar_today_rounded, color: Color(0xFFFF6B6B)),
                    ),
                    const SizedBox(width: 16),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('Date & Time', style: TextStyle(color: Colors.grey[400], fontSize: 12, fontWeight: FontWeight.w600)),
                        const SizedBox(height: 4),
                        Text(
                          DateFormat('yyyy-MM-dd HH:mm').format(_selectedDate),
                          style: const TextStyle(fontSize: 16, fontWeight: FontWeight.w600, color: Color(0xFF2D3436)),
                        ),
                      ],
                    ),
                    const Spacer(),
                    const Icon(Icons.arrow_forward_ios_rounded, size: 16, color: Colors.grey),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 24),
            _buildSectionHeader('备注', Icons.edit_note_rounded),
            const SizedBox(height: 12),
            Container(
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(20),
                boxShadow: [BoxShadow(color: Colors.black.withValues(alpha: 0.04), blurRadius: 20, offset: const Offset(0, 10))],
              ),
              child: TextField(
                controller: _noteController,
                decoration: const InputDecoration(
                  hintText: '记录当下的感受、使用的玩具...',
                  hintStyle: TextStyle(color: Colors.black26),
                  border: InputBorder.none,
                  enabledBorder: InputBorder.none,
                  focusedBorder: InputBorder.none,
                  contentPadding: EdgeInsets.all(20),
                ),
                maxLines: 4,
                style: const TextStyle(fontSize: 16),
              ),
            ),

            const SizedBox(height: 40),
            SizedBox(
              width: double.infinity,
              child: Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(20),
                  gradient: const LinearGradient(
                    colors: [Color(0xFF6B4EFF), Color(0xFF9279FF)],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFF6B4EFF).withValues(alpha: 0.4),
                      blurRadius: 20,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                child: ElevatedButton.icon(
                  onPressed: _submitData,
                  icon: const Icon(Icons.check_rounded),
                  label: const Text('保存记录'),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.transparent,
                    shadowColor: Colors.transparent,
                    padding: const EdgeInsets.symmetric(vertical: 20),
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                  ),
                ),
              ),
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title, IconData icon) {
    return Row(
      children: [
        Icon(icon, size: 20, color: const Color(0xFF6B4EFF)),
        const SizedBox(width: 8),
        Text(
          title,
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.bold,
            color: Color(0xFF2D3436)
          )
        ),
      ],
    );
  }
}

// Delete this class as it cause conflict and error, we use RoundSliderThumbShape instead
/*
class _CustomThumbShape extends SliderComponentShape {
  @override
  Size getPreferredSize(bool isEnabled, bool isDiscrete) => const Size(20, 20);

  @override
  void paint(
    PaintingContext context,
    Offset center, {
    required Animation<double> activationAnimation,
    required Animation<double> enableAnimation,
    required bool isDiscrete,
    required TextPainter labelPainter,
    required RenderBox parentBox,
    required SliderThemeData sliderTheme,
    required TextDirection textDirection,
    required double value,
    required double textScaleFactor,
    required Size sizeWithOverflow,
  }) {
    final Canvas canvas = context.canvas;

    // Shadow
    final Path shadowPath = Path()..addOval(Rect.fromCenter(center: center + const Offset(0, 4), width: 24, height: 24));
    canvas.drawShadow(shadowPath, Colors.black, 4, true);

    // Thumb
    final Paint paint = Paint()
      ..color = Colors.white
      ..style = PaintingStyle.fill;
    canvas.drawCircle(center, 12, paint);

    // Inner dot
    final Paint innerPaint = Paint()
      ..color = const Color(0xFF6B4EFF)
      ..style = PaintingStyle.fill;
    canvas.drawCircle(center, 5, innerPaint);
  }
}
*/
