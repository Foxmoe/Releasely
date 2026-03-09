import 'package:flutter/material.dart';
import 'package:releasely_app/main.dart'; // Import RecordPage
import 'package:releasely_app/api_service.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 0;
  final ApiService _apiService = ApiService();
  bool _isLoggedIn = false;

  @override
  void initState() {
    super.initState();
    _checkLoginStatus();
  }

  Future<void> _checkLoginStatus() async {
    final loggedIn = await _apiService.isLoggedIn();
    if (mounted) {
      setState(() {
        _isLoggedIn = loggedIn;
      });
    }
  }

  late final List<Widget> _widgetOptions = <Widget>[
    const RecordPage(), // 首页 - 记录
    const Center(child: Text('健康与安全 (开发中)')), // TODO: Feature 2
    const Center(child: Text('伴侣互动 (开发中)')), // TODO: Feature 3
    const Center(child: Text('数据报表 (开发中)')), // TODO: Feature 4
    const Center(child: Text('知识库 (开发中)')), // TODO: Feature 5
    _buildProfilePage(),
  ];

  Widget _buildProfilePage() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _isLoggedIn
              ? const CircleAvatar(
                  radius: 40,
                  child: Icon(Icons.person, size: 40),
                )
              : GestureDetector(
                onTap: () {
                     Navigator.of(context).push(MaterialPageRoute(builder: (context) => const LoginPage()));
                },
                child: const CircleAvatar(
                  radius: 40,
                  backgroundColor: Colors.grey,
                  child: Icon(Icons.person_outline, size: 40, color: Colors.white),
                ),
              ),
          const SizedBox(height: 16),
          _isLoggedIn
              ? const Text('已登录', style: TextStyle(fontSize: 18))
              : TextButton(
                  onPressed: () {
                     Navigator.of(context).push(MaterialPageRoute(builder: (context) => const LoginPage()));
                  },
                  child: const Text('点击登录/注册', style: TextStyle(fontSize: 18)),
                ),
           if (_isLoggedIn)
            Padding(
              padding: const EdgeInsets.only(top: 20.0),
              child: FilledButton(
                onPressed: () async {
                  await _apiService.logout();
                  if (mounted) {
                      setState(() {
                      _isLoggedIn = false;
                    });
                  }
                },
                child: const Text('退出登录'),
              ),
            )
        ],
      ),
    );
  }

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _selectedIndex,
        children: _widgetOptions,
      ),
      bottomNavigationBar: NavigationBar(
        onDestinationSelected: _onItemTapped,
        selectedIndex: _selectedIndex,
        destinations: const <Widget>[
          NavigationDestination(
            icon: Icon(Icons.edit_calendar),
            label: '记录',
          ),
          NavigationDestination(
            icon: Icon(Icons.health_and_safety),
            label: '健康',
          ),
          NavigationDestination(
            icon: Icon(Icons.favorite),
            label: '伴侣',
          ),
          NavigationDestination(
            icon: Icon(Icons.bar_chart),
            label: '报表',
          ),
          NavigationDestination(
            icon: Icon(Icons.menu_book),
            label: '知识',
          ),
          NavigationDestination(
            icon: Icon(Icons.person),
            label: '我的',
          ),
        ],
      ),
    );
  }
}
