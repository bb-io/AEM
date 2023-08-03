using System;
namespace Apps.AEM
{
	public class FolderResponse
	{
		public string Name { get; set; }
		public string Title { get; set; }
		public string Self { get; set; }
		public string Parent { get; set; }
		public string? Thumbnail { get; set; }
	};

	public class Credentials
	{
		public bool Ok { get; set; }
		public Integration Integration { get; set; }
		public int StatusCode { get; set; }
	}

	public class Integration
	{
		public string ImsEndpoint { get; set; }
		public string Metascopes { get; set; }
		public TechnicalAccount TechnicalAccount { get; set; }
		public string Email { get; set;}
		public string Id { get; set; }
		public string Org { get; set; }
		public string PrivateKey { get; set; }
		public string PublicKey { get; set; }
	}

	public class TechnicalAccount
	{
		public string ClientId { get; set; }
		public string ClientSecret { get; set; }
	}
}

