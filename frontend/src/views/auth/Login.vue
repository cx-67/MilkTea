<script setup>
import LoginForm from '../../components/auth/LoginForm.vue'
</script>

<template>
  <div class="auth-container">
    <!-- Background Decorations -->
    <div class="bg-decoration circle-1"></div>
    <div class="bg-decoration circle-2"></div>
    
    <!-- Milk Tea Image -->
    <div class="milk-tea-img-container">
      <img src="/奶茶.jpg" alt="Milk Tea" class="milk-tea-img" />
    </div>

    <div class="auth-card">
      <div class="card-header">
        <h1 class="title">今天你喝奶茶了吗</h1>
      </div>
      <LoginForm />
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: var(--mt-secondary);
  position: relative;
  overflow: hidden;
}

/* Background Circles */
.bg-decoration {
  position: absolute;
  border-radius: 50%;
  opacity: 0.6;
  z-index: 0;
}
.circle-1 {
  width: 600px;
  height: 600px;
  background: #FDE8D0;
  top: -200px;
  right: -100px;
}
.circle-2 {
  width: 400px;
  height: 400px;
  background: #E8D5C0;
  bottom: -100px;
  left: -100px;
}

/* Auth Card */
.auth-card {
  background: var(--mt-white);
  padding: 40px 50px;
  border-radius: 24px;
  box-shadow: var(--mt-shadow);
  width: 100%;
  max-width: 480px;
  z-index: 10;
  position: relative;
  text-align: center;
}

.title {
  font-size: 28px;
  color: var(--mt-text-main);
  margin-bottom: 30px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* Milk Tea Image */
.milk-tea-img-container {
  position: absolute;
  left: 10%;
  bottom: 10%;
  z-index: 5;
  width: 300px;
  height: auto;
  animation: float 3s ease-in-out infinite;
}

.milk-tea-img {
  width: 100%;
  height: auto;
  border-radius: 20px;
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  transform: rotate(-5deg);
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}
</style>