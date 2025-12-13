<script setup>
import RegisterForm from '../../components/auth/RegisterForm.vue'
</script>

<template>
  <div class="auth-container">
    <!-- Background Decorations -->
    <div class="bg-decoration circle-1"></div>
    <div class="bg-decoration circle-2"></div>
    
    <div class="auth-card">
      <div class="card-header">
        <h1 class="title">今天你喝奶茶了吗</h1>
      </div>
      <RegisterForm />
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
  width: 500px;
  height: 500px;
  background: #D4A574;
  opacity: 0.1;
  top: -100px;
  left: -100px;
}
.circle-2 {
  width: 700px;
  height: 700px;
  background: #FDE8D0;
  bottom: -200px;
  right: -200px;
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
</style>