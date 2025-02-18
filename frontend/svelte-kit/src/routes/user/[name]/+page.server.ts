import type { UserDTO } from '$lib/models/user/user';
import { makeRequest } from '$lib/server/apis/api';
import { HttpRequest } from '$lib/server/utils/util';
import { error } from '@sveltejs/kit';
import type { PageServerLoad } from './$types';

export const load = (async ({ params }) => {
  const response = await makeRequest({
    method: HttpRequest.GET,
    path: `/users/username/${params.name}`,
  });

  if ('error' in response) throw error(response.status, { message: response.error });

  return { user: response as UserDTO };
}) satisfies PageServerLoad;
