# ProCareerV2 📱

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Clean Architecture](https://img.shields.io/badge/Clean_Architecture-FF5722?style=for-the-badge&logo=architecture&logoColor=white)

</div>

## 📋 О проекте

ProCareerV2 — это современное Android-приложение для развития карьеры IT-специалистов. Платформа предоставляет инструменты для поиска вакансий, прохождения тестов на профессиональные навыки, отслеживания прогресса обучения и формирования персонализированного карьерного пути.

## ✨ Ключевые возможности

- 🔍 **Поиск вакансий** — поиск и фильтрация вакансий с возможностью просмотра детальной информации
- 📝 **Профессиональные тесты** — тесты для оценки и развития технических навыков
- 🛣️ **Персональная дорожная карта** — индивидуальный план развития навыков и компетенций
- 👤 **Управление профилем** — настройка профиля с указанием интересов и специализаций
- 🔐 **Авторизация и регистрация** — надежная система аутентификации пользователей

## 🚀 Технологический стек

### Основа
- **Kotlin** (JVM 11)
- **Android API** 24-34
- **Clean Architecture + MVVM**

### UI
- **Jetpack Compose**
- **Material 3**
- **Compose Navigation**

### Внедрение зависимостей и архитектура
- **Hilt**
- **Architecture Components**
- **StateFlow**

### Сеть
- **Retrofit**
- **OkHttp**
- **Gson**

### Хранение данных
- **DataStore**
- **Coil** (для изображений)

### Тестирование
- **JUnit**
- **Espresso**
- **UI Automator**

### Сборка
- **Gradle** (Kotlin DSL)
- **Android Gradle Plugin**

## 🏗️ Архитектура

Проект следует принципам Clean Architecture и разделен на три основных слоя:

### 1. Domain Layer
- **Models**: `User`, `Vacancy`, `Test`, `Question`, `Answer`, `Roadmap`
- **Repository Interfaces**: Определяют контракты для доступа к данным

### 2. Data Layer
- **Repository Implementations**: Конкретные реализации интерфейсов репозиториев
- **Remote**: API-клиенты, DTO и сетевые интерцепторы
- **Local**: Локальное хранилище данных

### 3. Presentation Layer
- **ViewModels**: Управление состоянием UI и бизнес-логикой
- **Screens**: Composable-функции для отображения UI
- **Navigation**: Единая система навигации

## 📱 Скриншоты

<div align="center">
<!-- Здесь будут размещены скриншоты приложения -->
</div>

## ⚙️ Установка и запуск

1. Клонируйте репозиторий:
```bash
git clone https://github.com/yourusername/ProCareerV2.git
```

2. Откройте проект в Android Studio (версии Arctic Fox или новее)

3. Синхронизируйте Gradle

4. Запустите на эмуляторе или физическом устройстве

## 🛠️ Планы развития

- [ ] Оптимизация времени загрузки
- [ ] Внедрение пагинации для списков
- [ ] Реализация кэширования данных
- [ ] Оптимизация сетевых запросов
- [ ] Добавление индикаторов загрузки
- [ ] Предварительная загрузка данных

<div align="center">
  <sub>Built with ❤️ by ProCareer Team</sub>
</div>
