<script lang="ts">
  import { enhance } from '$app/forms';
  import type { ActionData } from './$types';

  export let form: ActionData;
</script>

<div class="hero min-h-screen">
  <div class="hero-content text-center">
    <div class="max-w-md">
      <form class="flex flex-col gap-4" method="POST" action="?/signUp" use:enhance>
        <div class="form-control w-full max-w-xs">
          <label for="firstName" class="label">
            <span class="label-text">First name</span>
          </label>
          <input
            type="text"
            id="firstName"
            name="firstName"
            class="input-bordered input w-full max-w-xs"
          />
          {#if form?.errors?.firstName}
            <label for="firstName" class="label">
              <span class="label-text text-error">{form.errors.firstName[0]}</span>
            </label>
          {/if}
        </div>

        <div class="form-control w-full max-w-xs">
          <label for="lastName" class="label">
            <span class="label-text">Last name</span>
          </label>
          <input
            type="text"
            id="lastName"
            name="lastName"
            class="input-bordered input w-full max-w-xs"
          />
          {#if form?.errors?.lastName}
            <label for="lastName" class="label">
              <span class="label-text text-error">{form.errors.lastName[0]}</span>
            </label>
          {/if}
        </div>

        <div class="form-control w-full max-w-xs">
          <label for="username" class="label">
            <span class="label-text">Username</span>
          </label>
          <input
            type="text"
            id="username"
            name="username"
            class="input-bordered input w-full max-w-xs"
          />
          {#if form?.errors?.username}
            <label for="username" class="label">
              <span class="label-text text-error">{form.errors.username[0]}</span>
            </label>
          {/if}
        </div>

        <div class="form-control w-full max-w-xs">
          <label for="email" class="label">
            <span class="label-text">Email</span>
          </label>
          <input
            type="email"
            id="email"
            name="email"
            class="input-bordered input w-full max-w-xs"
          />
          {#if form?.errors?.email}
            <label for="email" class="label">
              <span class="label-text text-error">{form.errors.email[0]}</span>
            </label>
          {/if}
        </div>

        <div class="form-control w-full max-w-xs">
          <label for="password" class="label">
            <span class="label-text">Password</span>
          </label>
          <input
            type="password"
            id="password"
            name="password"
            class="input-bordered input w-full max-w-xs"
          />
          {#if form?.errors?.password}
            <label for="password" class="label">
              <span class="label-text text-error">{form.errors.password[0]}</span>
            </label>
          {/if}
        </div>

        <div class="form-control w-full max-w-xs">
          <label for="confirmPassword" class="label">
            <span class="label-text">Confirm password</span>
          </label>
          <input
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            class="input-bordered input w-full max-w-xs"
          />
          {#if form?.errors?.confirmPassword}
            <label for="confirmPassword" class="label">
              <span class="label-text text-error">{form.errors.confirmPassword[0]}</span>
            </label>
          {/if}
        </div>

        {#if form?.errorMessage}
          {#each form.errorMessage.details as error}
            <div class="flex">
              <p class="label-text text-error">{error.message}</p>
            </div>
          {/each}
        {/if}

        <button class="btn-primary btn">Sign up</button>
      </form>
    </div>
  </div>
</div>
