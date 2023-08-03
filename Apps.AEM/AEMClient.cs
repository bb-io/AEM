using System;
using Blackbird.Applications.Sdk.Common.Authentication;
using RestSharp;

namespace Apps.AEM
{
	public class AEMClient : RestClient
	{
		public AEMClient(IEnumerable<AuthenticationCredentialsProvider> authenticationCredentialsProviders)
            : base
            (
                new RestClientOptions() { ThrowOnAnyError = true, BaseUrl = GetUri(authenticationCredentialsProviders) }
            ) { }

        private static Uri GetUri(IEnumerable<AuthenticationCredentialsProvider> authenticationCredentialsProvider)
        {
            var domainName = authenticationCredentialsProvider.First(v => v.KeyName == "domainName").Value;
            return new Uri($"https://{domainName}");

        }
    }
}

