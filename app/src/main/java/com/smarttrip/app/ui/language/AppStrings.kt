package com.smarttrip.app.ui.language

data class AppStrings(
    val landingTitle: String,
    val landingSubtitle: String,
    val featureAI: String,
    val featureCompa: String,
    val featureInspiration: String,
    val btnGuestLogin: String,
    val btnSignIn: String,
    val homeHeroTitle: String,
    val homeHeroSubtitle: String,
    val navHome: String,
    val navFavorites: String,
    val navHistory: String,
    val navProfile: String
) {
    companion object {
        fun forLanguage(lang: AppLanguage): AppStrings = when (lang) {
            AppLanguage.FRENCH -> AppStrings(
                landingTitle = "Voyagez plus\nintelligement",
                landingSubtitle = "Score IA, prédictions de prix et\ndécouverte de destinations sur mesure.",
                featureAI = "Score IA",
                featureCompa = "500+ compa.",
                featureInspiration = "Inspiration",
                btnGuestLogin = "Connexion Invité",
                btnSignIn = "Se connecter",
                homeHeroTitle = "Trouvez les meilleurs vols",
                homeHeroSubtitle = "Score IA • Prédictions ML • 500+ compagnies",
                navHome = "Accueil",
                navFavorites = "Favoris",
                navHistory = "Historique",
                navProfile = "Profil"
            )
            AppLanguage.ENGLISH -> AppStrings(
                landingTitle = "Travel smarter",
                landingSubtitle = "AI scores, price predictions and\ncustomized destination discovery.",
                featureAI = "AI Score",
                featureCompa = "500+ airlines",
                featureInspiration = "Inspiration",
                btnGuestLogin = "Guest Login",
                btnSignIn = "Sign In",
                homeHeroTitle = "Find the best flights",
                homeHeroSubtitle = "AI Score • ML Predictions • 500+ airlines",
                navHome = "Home",
                navFavorites = "Favorites",
                navHistory = "History",
                navProfile = "Profile"
            )
        }
    }
}
