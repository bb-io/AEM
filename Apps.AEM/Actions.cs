using System;
using Blackbird.Applications.Sdk.Common;
using Blackbird.Applications.Sdk.Common.Actions;
using Blackbird.Applications.Sdk.Common.Authentication;
using RestSharp;
using Jose;

namespace Apps.AEM
{
	[ActionList]
	public class Actions
	{
		[Action]
		public async FolderResponse getFolder(IEnumerable<AuthenticationCredentialsProvider> authenticationCredentialsProviders, [ActionParameter] string folderName)
		{
            var domainName = authenticationCredentialsProviders.First(v => v.KeyName == "domainName").Value;
            var credentialsJsonString = authenticationCredentialsProviders.First(v => v.KeyName == "credentialsJSON").Value;
            Credentials credentialsJsonDict;

            try
            {
                if (credentialsJsonString == null) throw new Exception("null credentials");
                credentialsJsonDict = System.Text.Json.JsonSerializer.Deserialize<Credentials>(credentialsJsonString);
            }
            catch
            {
                throw new Exception("error serializing json");
            }

            var client = new AEMClient(authenticationCredentialsProviders: authenticationCredentialsProviders);
			var request = new RestRequest($"/api/assets/{folderName}", Method.Get);

            string authToken;

            Dictionary<string, object> jwtPayload = new Dictionary<string, object>()
            {
                {"iss", credentialsJsonDict.Integration.Org },
                {"sub", credentialsJsonDict.Integration.Id},
                {"exp", DateTime.UtcNow.AddHours(8).Ticks / TimeSpan.TicksPerMillisecond },
                {"aud", $"https://{credentialsJsonDict.Integration.ImsEndpoint}/c/{credentialsJsonDict.Integration.TechnicalAccount.ClientId}" }
            };

            var metascopes = credentialsJsonDict.Integration.Metascopes.Split(",");

            foreach (var metascope in metascopes)
            {
                jwtPayload[$"https://{credentialsJsonDict.Integration.ImsEndpoint}/s/{metascope}"] = true;
            }

            var jwtToken = Jose.JWT.Encode(jwtPayload, credentialsJsonDict.Integration.PrivateKey, JwsAlgorithm.RS256);

            var authClient = new RestClient(new RestClientOptions($"https://{credentialsJsonDict.Integration.ImsEndpoint}"));
            var authRequest = new RestRequest("/ims/exchange/jwt", Method.Post);
            authRequest.AddHeader("content-type", "application/x-www-form-urlencoded");
            authRequest.AddQueryParameter("client_id", credentialsJsonDict.Integration.TechnicalAccount.ClientId);
            authRequest.AddQueryParameter("client_secret", credentialsJsonDict.Integration.TechnicalAccount.ClientSecret);
            authRequest.AddQueryParameter("jwt_token", jwtToken);

            var resp = authClient.Execute(authRequest);
            authToken = resp.Content;

            request.AddHeader("Authorization", $"Bearer {authToken}");

            var response = client.Execute<FolderResponse>(request);
            if (response.Data == null)
            {
                throw new Exception("response data was null");
            }

            return response.Data;
        }
	}
}

